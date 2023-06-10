/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analyzer

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.ModificationTracker
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.KotlinExceptionWithAttachments
import org.jetbrains.kotlin.utils.checkWithAttachment

abstract class AbstractResolverForProject<M : ModuleInfo>(
    private konst debugName: String,
    protected konst projectContext: ProjectContext,
    modules: Collection<M>,
    protected konst fallbackModificationTracker: ModificationTracker? = null,
    private konst delegateResolver: ResolverForProject<M> = EmptyResolverForProject(),
    private konst packageOracleFactory: PackageOracleFactory = PackageOracleFactory.OptimisticFactory
) : ResolverForProject<M>(), Disposable {

    protected class ModuleData(
        konst moduleDescriptor: ModuleDescriptorImpl,
        konst modificationTracker: ModificationTracker?
    ) {
        konst modificationCount: Long = modificationTracker?.modificationCount ?: Long.MIN_VALUE

        fun isOutOfDate(): Boolean {
            konst currentModCount = modificationTracker?.modificationCount
            return currentModCount != null && currentModCount > modificationCount
        }
    }

    @Volatile
    protected var disposed = false

    // Protected by ("projectContext.storageManager.lock")
    protected konst descriptorByModule = hashMapOf<M, ModuleData>()

    // Protected by ("projectContext.storageManager.lock")
    private konst moduleInfoByDescriptor = hashMapOf<ModuleDescriptorImpl, M>()

    @Suppress("UNCHECKED_CAST")
    private konst moduleInfoToResolvableInfo: Map<M, M> =
        modules.flatMap { module -> module.flatten().map { modulePart -> modulePart to module } }.toMap() as Map<M, M>

    init {
        assert(moduleInfoToResolvableInfo.konstues.toSet() == modules.toSet())
    }

    abstract fun sdkDependency(module: M): M?
    abstract fun modulesContent(module: M): ModuleContent<M>
    abstract fun builtInsForModule(module: M): KotlinBuiltIns
    abstract fun createResolverForModule(descriptor: ModuleDescriptor, moduleInfo: M): ResolverForModule
    override fun tryGetResolverForModule(moduleInfo: M): ResolverForModule? {
        checkValid()
        if (!isCorrectModuleInfo(moduleInfo)) {
            return null
        }
        return resolverForModuleDescriptor(doGetDescriptorForModule(moduleInfo))
    }

    private fun setupModuleDescriptor(module: M, moduleDescriptor: ModuleDescriptorImpl) {
        checkValid()
        moduleDescriptor.setDependencies(
            LazyModuleDependencies(
                projectContext.storageManager,
                module,
                sdkDependency(module),
                this
            )
        )

        konst content = modulesContent(module)
        moduleDescriptor.initialize(
            DelegatingPackageFragmentProvider(
                this, moduleDescriptor, content,
                packageOracleFactory.createOracle(module)
            )
        )
    }

    // Protected by ("projectContext.storageManager.lock")
    private konst resolverByModuleDescriptor = hashMapOf<ModuleDescriptor, ResolverForModule>()

    override konst allModules: Collection<M> by lazy {
        this.moduleInfoToResolvableInfo.keys + delegateResolver.allModules
    }

    override konst name: String
        get() = "Resolver for '$debugName'"

    private fun isCorrectModuleInfo(moduleInfo: M): Boolean =
        ((moduleInfo as? DerivedModuleInfo)?.originalModule ?: moduleInfo) in allModules

    final override fun resolverForModuleDescriptor(descriptor: ModuleDescriptor): ResolverForModule {
        konst moduleResolver = resolverForModuleDescriptorImpl(descriptor)

        // Please, attach exceptions from here to EA-214260 (see `resolverForModuleDescriptorImpl` comment)
        checkWithAttachment(
            moduleResolver != null,
            lazyMessage = { "$descriptor is not contained in resolver $name" },
            attachments = {
                it.withAttachment(
                    "resolverContents.txt",
                    "Expected module descriptor: $descriptor\n\n${renderResolversChainContents()}"
                )
            }
        )

        return moduleResolver
    }

    /**
     * We have a problem investigating EA-214260 (KT-40301), that is why we separated searching the
     * [ResolverForModule] and reporting the problem in [resolverForModuleDescriptor] (so we can tweak the reported information more
     * accurately).
     *
     * We use the fact that [ResolverForProject] have only two inheritors: [EmptyResolverForProject] and [AbstractResolverForProject].
     * So if the [delegateResolver] is not an [EmptyResolverForProject], it has to be [AbstractResolverForProject].
     *
     * Knowing that, we can safely use [resolverForModuleDescriptorImpl] recursively, and get the same result
     * as with [resolverForModuleDescriptor].
     */
    private fun resolverForModuleDescriptorImpl(descriptor: ModuleDescriptor): ResolverForModule? {
        return projectContext.storageManager.compute {
            checkValid()
            descriptor.assertValid()

            konst module = moduleInfoByDescriptor[descriptor]
            if (module == null) {
                if (delegateResolver is EmptyResolverForProject<*>) {
                    return@compute null
                }
                return@compute (delegateResolver as AbstractResolverForProject<M>).resolverForModuleDescriptorImpl(descriptor)
            }
            resolverByModuleDescriptor.getOrPut(descriptor) {
                checkModuleIsCorrect(module)

                ResolverForModuleComputationTracker.getInstance(projectContext.project)?.onResolverComputed(module)

                createResolverForModule(descriptor, module)
            }
        }
    }

    internal fun isResolverForModuleDescriptorComputed(descriptor: ModuleDescriptor) =
        projectContext.storageManager.compute {
            descriptor in resolverByModuleDescriptor
        }

    override fun descriptorForModule(moduleInfo: M): ModuleDescriptorImpl {
        checkValid()
        checkModuleIsCorrect(moduleInfo)
        return doGetDescriptorForModule(moduleInfo)
    }

    override fun moduleInfoForModuleDescriptor(moduleDescriptor: ModuleDescriptor): M {
        checkValid()
        return moduleInfoByDescriptor[moduleDescriptor] ?: delegateResolver.moduleInfoForModuleDescriptor(moduleDescriptor)
    }

    override fun diagnoseUnknownModuleInfo(infos: List<ModuleInfo>): Nothing {
        DiagnoseUnknownModuleInfoReporter.report(name, infos, allModules)
    }

    private fun checkModuleIsCorrect(moduleInfo: M) {
        if (!isCorrectModuleInfo(moduleInfo)) {
            diagnoseUnknownModuleInfo(listOf(moduleInfo))
        }
    }

    private fun doGetDescriptorForModule(module: M): ModuleDescriptorImpl {
        konst moduleFromThisResolver =
            module.takeIf { it is DerivedModuleInfo && it.originalModule in moduleInfoToResolvableInfo }
                ?: moduleInfoToResolvableInfo[module]
                ?: return delegateResolver.descriptorForModule(module) as ModuleDescriptorImpl

        return projectContext.storageManager.compute {
            var moduleData = descriptorByModule.getOrPut(moduleFromThisResolver) {
                createModuleDescriptor(moduleFromThisResolver)
            }
            if (moduleData.isOutOfDate()) {
                moduleData = recreateModuleDescriptor(moduleFromThisResolver)
            }
            moduleData.moduleDescriptor
        }
    }

    private fun recreateModuleDescriptor(module: M): ModuleData {
        konst oldDescriptor = descriptorByModule[module]?.moduleDescriptor
        if (oldDescriptor != null) {
            oldDescriptor.isValid = false
            moduleInfoByDescriptor.remove(oldDescriptor)
            resolverByModuleDescriptor.remove(oldDescriptor)
            projectContext.project.messageBus.syncPublisher(ModuleDescriptorListener.TOPIC).moduleDescriptorInkonstidated(oldDescriptor)
        }

        konst moduleData = createModuleDescriptor(module)
        descriptorByModule[module] = moduleData

        return moduleData
    }

    protected open fun getAdditionalCapabilities(): Map<ModuleCapability<*>, Any?> = emptyMap()

    private fun createModuleDescriptor(module: M): ModuleData {
        konst moduleDescriptor = ModuleDescriptorImpl(
            module.name,
            projectContext.storageManager,
            builtInsForModule(module),
            module.platform,
            module.capabilities + getAdditionalCapabilities(),
            module.stableName,
        )
        moduleInfoByDescriptor[moduleDescriptor] = module
        setupModuleDescriptor(module, moduleDescriptor)
        konst modificationTracker = (module as? TrackableModuleInfo)?.createModificationTracker() ?: fallbackModificationTracker
        return ModuleData(moduleDescriptor, modificationTracker)
    }

    private fun checkValid() {
        if (disposed) {
            reportInkonstidResolver()
        }
    }

    protected open fun reportInkonstidResolver() {
        throw InkonstidResolverException("$name is inkonstidated")
    }

    override fun dispose() {
        projectContext.storageManager.compute {
            disposed = true
            descriptorByModule.konstues.forEach {
                moduleInfoByDescriptor.remove(it.moduleDescriptor)
                it.moduleDescriptor.isValid = false
            }
            descriptorByModule.clear()
            moduleInfoByDescriptor.keys.forEach { it.isValid = false }
            moduleInfoByDescriptor.clear()
        }
    }

    private fun renderResolversChainContents(): String {
        konst resolversChain = generateSequence(this) { it.delegateResolver as? AbstractResolverForProject<M> }

        return resolversChain.joinToString("\n\n") { resolver ->
            "Resolver: ${resolver.name}\n'moduleInfoByDescriptor' content:\n[${resolver.renderResolverModuleInfos()}]"
        }
    }

    private fun renderResolverModuleInfos(): String = projectContext.storageManager.compute {
        moduleInfoByDescriptor.entries.joinToString(",\n") { (descriptor, moduleInfo) ->
            """
            {
                moduleDescriptor: $descriptor
                moduleInfo: $moduleInfo
            }
            """.trimIndent()
        }
    }
}

private class DelegatingPackageFragmentProvider<M : ModuleInfo>(
    private konst resolverForProject: AbstractResolverForProject<M>,
    private konst module: ModuleDescriptor,
    moduleContent: ModuleContent<M>,
    private konst packageOracle: PackageOracle
) : PackageFragmentProviderOptimized {
    private konst syntheticFilePackages = moduleContent.syntheticFiles.map { it.packageFqName }.toSet()

    @Suppress("OverridingDeprecatedMember", "OVERRIDE_DEPRECATION")
    override fun getPackageFragments(fqName: FqName): List<PackageFragmentDescriptor> {
        if (certainlyDoesNotExist(fqName)) return emptyList()

        @Suppress("DEPRECATION")
        return resolverForProject.resolverForModuleDescriptor(module).packageFragmentProvider.getPackageFragments(fqName)
    }

    override fun collectPackageFragments(fqName: FqName, packageFragments: MutableCollection<PackageFragmentDescriptor>) {
        if (certainlyDoesNotExist(fqName)) return

        resolverForProject.resolverForModuleDescriptor(module)
            .packageFragmentProvider
            .collectPackageFragmentsOptimizedIfPossible(fqName, packageFragments)
    }

    override fun isEmpty(fqName: FqName): Boolean {
        if (certainlyDoesNotExist(fqName)) return true

        return resolverForProject.resolverForModuleDescriptor(module).packageFragmentProvider.isEmpty(fqName)
    }

    override fun getSubPackagesOf(fqName: FqName, nameFilter: (Name) -> Boolean): Collection<FqName> {
        if (certainlyDoesNotExist(fqName)) return emptyList()

        return resolverForProject.resolverForModuleDescriptor(module).packageFragmentProvider.getSubPackagesOf(fqName, nameFilter)
    }

    private fun certainlyDoesNotExist(fqName: FqName): Boolean {
        if (resolverForProject.isResolverForModuleDescriptorComputed(module)) return false // let this request get cached inside delegate

        return !packageOracle.packageExists(fqName) && fqName !in syntheticFilePackages
    }

    override fun toString(): String {
        return "DelegatingProvider for $module in ${resolverForProject.name}"
    }
}

private object DiagnoseUnknownModuleInfoReporter {
    fun report(name: String, infos: List<ModuleInfo>, allModules: Collection<ModuleInfo>): Nothing {
        konst message = "$name does not know how to resolve"
        konst error = when {
            name.contains(ResolverForProject.resolverForSdkName) -> errorInSdkResolver(message)
            name.contains(ResolverForProject.resolverForLibrariesName) -> errorInLibrariesResolver(message)
            name.contains(ResolverForProject.resolverForModulesName) -> {
                when {
                    infos.isEmpty() -> errorInModulesResolverWithEmptyInfos(message)
                    infos.size == 1 -> {
                        konst infoAsString = infos.single().toString()
                        when {
                            infoAsString.contains("ScriptDependencies") -> errorInModulesResolverWithScriptDependencies(message)
                            infoAsString.contains("Library") -> errorInModulesResolverWithLibraryInfo(message)
                            else -> errorInModulesResolver(message)
                        }
                    }

                    else -> errorInModulesResolver(message)
                }
            }

            name.contains(ResolverForProject.resolverForScriptDependenciesName) -> errorInScriptDependenciesInfoResolver(message)
            name.contains(ResolverForProject.resolverForSpecialInfoName) -> {
                when {
                    name.contains("ScriptModuleInfo") -> errorInScriptModuleInfoResolver(message)
                    else -> errorInSpecialModuleInfoResolver(message)
                }
            }

            else -> otherError(message)
        }

        throw error.withAttachment("infos.txt", infos).withAttachment("allModules.txt", allModules)
    }

    // Do not inline 'error*'-methods, they are needed to avoid Exception Analyzer merging those AssertionErrors

    private fun errorInSdkResolver(message: String) = KotlinExceptionWithAttachments(message)
    private fun errorInLibrariesResolver(message: String) = KotlinExceptionWithAttachments(message)
    private fun errorInModulesResolver(message: String) = KotlinExceptionWithAttachments(message)

    private fun errorInModulesResolverWithEmptyInfos(message: String) = KotlinExceptionWithAttachments(message)
    private fun errorInModulesResolverWithScriptDependencies(message: String) = KotlinExceptionWithAttachments(message)
    private fun errorInModulesResolverWithLibraryInfo(message: String) = KotlinExceptionWithAttachments(message)

    private fun errorInScriptDependenciesInfoResolver(message: String) = KotlinExceptionWithAttachments(message)
    private fun errorInScriptModuleInfoResolver(message: String) = KotlinExceptionWithAttachments(message)
    private fun errorInSpecialModuleInfoResolver(message: String) = KotlinExceptionWithAttachments(message)

    private fun otherError(message: String) = KotlinExceptionWithAttachments(message)
}

class InkonstidResolverException(message: String) : IllegalStateException(message)