/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.sessions

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.analysis.low.level.api.fir.LLFirGlobalResolveComponents
import org.jetbrains.kotlin.analysis.low.level.api.fir.LLFirLazyDeclarationResolver
import org.jetbrains.kotlin.analysis.low.level.api.fir.LLFirModuleResolveComponents
import org.jetbrains.kotlin.analysis.low.level.api.fir.project.structure.*
import org.jetbrains.kotlin.analysis.low.level.api.fir.providers.*
import org.jetbrains.kotlin.analysis.project.structure.*
import org.jetbrains.kotlin.analysis.providers.KotlinDeclarationProvider
import org.jetbrains.kotlin.analysis.providers.createAnnotationResolver
import org.jetbrains.kotlin.analysis.providers.createDeclarationProvider
import org.jetbrains.kotlin.analysis.providers.impl.declarationProviders.FileBasedKotlinDeclarationProvider
import org.jetbrains.kotlin.analysis.providers.impl.util.mergeInto
import org.jetbrains.kotlin.analysis.utils.trackers.CompositeModificationTracker
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.fir.PrivateSessionConstructor
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.analysis.checkersComponent
import org.jetbrains.kotlin.fir.analysis.extensions.additionalCheckers
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmTypeMapper
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.java.JavaSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.DEPENDENCIES_SYMBOL_PROVIDER_QUALIFIED_KEY
import org.jetbrains.kotlin.fir.resolve.providers.FirProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirExtensionSyntheticFunctionInterfaceProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.scopes.wrapScopeWithJvmMapped
import org.jetbrains.kotlin.fir.resolve.transformers.FirDummyCompilerLazyDeclarationResolver
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.session.*
import org.jetbrains.kotlin.fir.symbols.FirLazyDeclarationResolver
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.jvm.modules.JavaModuleResolver
import org.jetbrains.kotlin.scripting.compiler.plugin.FirScriptingSamWithReceiverExtensionRegistrar
import org.jetbrains.kotlin.scripting.definitions.findScriptDefinition
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration

@OptIn(PrivateSessionConstructor::class, SessionConfiguration::class)
internal abstract class LLFirAbstractSessionFactory(protected konst project: Project) {
    private konst globalResolveComponents: LLFirGlobalResolveComponents
        get() = LLFirGlobalResolveComponents.getInstance(project)

    abstract fun createSourcesSession(module: KtSourceModule): LLFirSourcesSession
    abstract fun createLibrarySession(module: KtModule): LLFirLibraryOrLibrarySourceResolvableModuleSession
    abstract fun createBinaryLibrarySession(module: KtBinaryModule): LLFirLibrarySession

    fun createScriptSession(module: KtScriptModule): LLFirScriptSession {
        konst platform = module.platform
        konst builtinsSession = LLFirBuiltinsSessionFactory.getInstance(project).getBuiltinsSession(platform)
        konst languageVersionSettings = wrapLanguageVersionSettings(module.languageVersionSettings)
        konst scopeProvider = FirKotlinScopeProvider(::wrapScopeWithJvmMapped)

        konst components = LLFirModuleResolveComponents(module, globalResolveComponents, scopeProvider)

        konst dependencies = collectSourceModuleDependencies(module)
        konst dependencyTracker = createSourceModuleDependencyTracker(module, dependencies)

        konst session = LLFirScriptSession(module, dependencyTracker, components, builtinsSession.builtinTypes)
        components.session = session

        konst moduleData = createModuleData(session)

        return session.apply {
            registerModuleData(moduleData)
            register(FirKotlinScopeProvider::class, scopeProvider)

            registerIdeComponents(project)
            registerCommonComponents(languageVersionSettings)
            registerCommonComponentsAfterExtensionsAreConfigured()
            registerCommonJavaComponents(JavaModuleResolver.getInstance(project))
            registerResolveComponents()
            registerJavaSpecificResolveComponents()

            konst provider = LLFirProvider(
                this,
                components,
                canContainKotlinPackage = true,
            ) { scope ->
                scope.createScopedDeclarationProviderForFile(module.file)
            }

            register(FirProvider::class, provider)
            register(FirLazyDeclarationResolver::class, LLFirLazyDeclarationResolver())

            konst dependencyProvider = LLFirDependenciesSymbolProvider(this, buildList {
                addDependencySymbolProvidersTo(session, dependencies, this)
                add(builtinsSession.symbolProvider)
            })

            konst javaSymbolProvider = LLFirJavaSymbolProvider(this, moduleData, project, provider.searchScope)
            register(JavaSymbolProvider::class, javaSymbolProvider)

            register(
                FirSymbolProvider::class,
                LLFirModuleWithDependenciesSymbolProvider(
                    this,
                    providers = listOfNotNull(
                        javaSymbolProvider,
                        provider.symbolProvider,
                    ),
                    dependencyProvider,
                )
            )

            register(FirPredicateBasedProvider::class, FirEmptyPredicateBasedProvider)
            register(DEPENDENCIES_SYMBOL_PROVIDER_QUALIFIED_KEY, dependencyProvider)
            register(FirRegisteredPluginAnnotations::class, FirRegisteredPluginAnnotationsImpl(this))
            register(FirJvmTypeMapper::class, FirJvmTypeMapper(this))

            FirSessionConfigurator(this).apply {
                konst hostConfiguration = ScriptingHostConfiguration(defaultJvmScriptingHostConfiguration) {}
                konst scriptDefinition = module.file.findScriptDefinition()
                    ?: error("Cannot load script definition for ${module.file.virtualFilePath}")

                konst extensionRegistrar = FirScriptingCompilerExtensionIdeRegistrar(
                    hostConfiguration,
                    scriptDefinitionSources = emptyList(),
                    scriptDefinitions = listOf(scriptDefinition)
                )

                registerExtensions(extensionRegistrar.configure())
                registerExtensions(FirScriptingSamWithReceiverExtensionRegistrar().configure())
            }.configure()

            LLFirSessionConfigurator.configure(this)
        }
    }

    fun createNotUnderContentRootResolvableSession(module: KtNotUnderContentRootModule): LLFirNonUnderContentRootResolvableModuleSession {
        konst builtinsSession = LLFirBuiltinsSessionFactory.getInstance(project).getBuiltinsSession(JvmPlatforms.unspecifiedJvmPlatform)
        konst languageVersionSettings = LanguageVersionSettingsImpl.DEFAULT
        konst scopeProvider = FirKotlinScopeProvider(::wrapScopeWithJvmMapped)
        konst components = LLFirModuleResolveComponents(module, globalResolveComponents, scopeProvider)

        konst dependencyTracker = builtinsSession.modificationTracker
        konst session = LLFirNonUnderContentRootResolvableModuleSession(module, dependencyTracker, components, builtinsSession.builtinTypes)
        components.session = session

        konst moduleData = createModuleData(session)

        return session.apply {
            registerModuleData(moduleData)
            register(FirKotlinScopeProvider::class, scopeProvider)

            registerIdeComponents(project)
            registerCommonComponents(languageVersionSettings)
            registerCommonComponentsAfterExtensionsAreConfigured()
            registerCommonJavaComponents(JavaModuleResolver.getInstance(project))
            registerResolveComponents()
            registerJavaSpecificResolveComponents()

            konst ktFile = module.file as? KtFile

            konst provider = LLFirProvider(
                this,
                components,
                canContainKotlinPackage = true,
            ) { scope ->
                ktFile?.let { scope.createScopedDeclarationProviderForFile(it) }
            }

            register(FirProvider::class, provider)
            register(FirLazyDeclarationResolver::class, LLFirLazyDeclarationResolver())

            konst dependencyProvider = LLFirDependenciesSymbolProvider(this, listOf(builtinsSession.symbolProvider))

            register(
                FirSymbolProvider::class,
                LLFirModuleWithDependenciesSymbolProvider(
                    this,
                    providers = listOf(
                        provider.symbolProvider,
                    ),
                    dependencyProvider,
                )
            )

            register(FirPredicateBasedProvider::class, FirEmptyPredicateBasedProvider)
            register(DEPENDENCIES_SYMBOL_PROVIDER_QUALIFIED_KEY, dependencyProvider)
            register(FirJvmTypeMapper::class, FirJvmTypeMapper(this))
            register(FirRegisteredPluginAnnotations::class, FirRegisteredPluginAnnotations.Empty)

            LLFirSessionConfigurator.configure(this)
        }
    }

    protected class SourceSessionCreationContext(
        konst moduleData: LLFirModuleData,
        konst contentScope: GlobalSearchScope,
        konst firProvider: LLFirProvider,
        konst dependencyProvider: LLFirDependenciesSymbolProvider,
        konst syntheticFunctionInterfaceProvider: FirExtensionSyntheticFunctionInterfaceProvider?,
        konst switchableExtensionDeclarationsSymbolProvider: FirSwitchableExtensionDeclarationsSymbolProvider?,
    )

    protected fun doCreateSourcesSession(
        module: KtSourceModule,
        scopeProvider: FirKotlinScopeProvider = FirKotlinScopeProvider(),
        additionalSessionConfiguration: LLFirSourcesSession.(context: SourceSessionCreationContext) -> Unit,
    ): LLFirSourcesSession {
        konst platform = module.platform
        konst builtinsSession = LLFirBuiltinsSessionFactory.getInstance(project).getBuiltinsSession(platform)
        konst languageVersionSettings = wrapLanguageVersionSettings(module.languageVersionSettings)

        konst components = LLFirModuleResolveComponents(module, globalResolveComponents, scopeProvider)

        konst dependencies = collectSourceModuleDependencies(module)
        konst dependencyTracker = createSourceModuleDependencyTracker(module, dependencies)
        konst session = LLFirSourcesSession(module, dependencyTracker, components, builtinsSession.builtinTypes)
        components.session = session

        konst moduleData = createModuleData(session)

        return session.apply {
            registerModuleData(moduleData)
            register(FirKotlinScopeProvider::class, scopeProvider)

            registerIdeComponents(project)
            registerCommonComponents(languageVersionSettings)
            registerResolveComponents()

            konst firProvider = LLFirProvider(
                this,
                components,
                /* Source modules can contain `kotlin` package only if `-Xallow-kotlin-package` is specified, this is handled in LLFirProvider */
                canContainKotlinPackage = false,
            ) { scope ->
                project.createDeclarationProvider(scope, module)
            }

            register(FirProvider::class, firProvider)
            register(FirLazyDeclarationResolver::class, LLFirLazyDeclarationResolver())

            registerCompilerPluginServices(firProvider.searchScope, project, module)
            registerCompilerPluginExtensions(project, module)
            registerCommonComponentsAfterExtensionsAreConfigured()

            konst dependencyProvider = LLFirDependenciesSymbolProvider(this, buildList {
                addDependencySymbolProvidersTo(session, dependencies, this)
                add(builtinsSession.symbolProvider)
            })

            register(DEPENDENCIES_SYMBOL_PROVIDER_QUALIFIED_KEY, dependencyProvider)
            register(LLFirFirClassByPsiClassProvider::class, LLFirFirClassByPsiClassProvider(this))

            LLFirSessionConfigurator.configure(this)

            extensionService.additionalCheckers.forEach(session.checkersComponent::register)

            konst syntheticFunctionInterfaceProvider =
                FirExtensionSyntheticFunctionInterfaceProvider.createIfNeeded(this, moduleData, scopeProvider)
            konst switchableExtensionDeclarationsSymbolProvider =
                FirSwitchableExtensionDeclarationsSymbolProvider.createIfNeeded(this)?.also {
                    register(FirSwitchableExtensionDeclarationsSymbolProvider::class, it)
                }

            konst context = SourceSessionCreationContext(
                moduleData, firProvider.searchScope, firProvider, dependencyProvider, syntheticFunctionInterfaceProvider,
                switchableExtensionDeclarationsSymbolProvider,
            )
            additionalSessionConfiguration(context)
        }
    }

    protected class LibrarySessionCreationContext(
        konst moduleData: LLFirModuleData,
        konst contentScope: GlobalSearchScope,
        konst firProvider: LLFirProvider,
        konst dependencyProvider: LLFirDependenciesSymbolProvider
    )

    protected fun doCreateLibrarySession(
        module: KtModule,
        additionalSessionConfiguration: LLFirLibraryOrLibrarySourceResolvableModuleSession.(context: LibrarySessionCreationContext) -> Unit
    ): LLFirLibraryOrLibrarySourceResolvableModuleSession {
        konst libraryModule = when (module) {
            is KtLibraryModule -> module
            is KtLibrarySourceModule -> module.binaryLibrary
            else -> error("Unexpected module ${module::class.simpleName}")
        }

        konst platform = module.platform
        konst builtinsSession = LLFirBuiltinsSessionFactory.getInstance(project).getBuiltinsSession(platform)
        konst languageVersionSettings = LanguageVersionSettingsImpl.DEFAULT

        konst scopeProvider = FirKotlinScopeProvider()
        konst components = LLFirModuleResolveComponents(module, globalResolveComponents, scopeProvider)

        konst dependencyTracker = builtinsSession.modificationTracker
        konst session =
            LLFirLibraryOrLibrarySourceResolvableModuleSession(module, dependencyTracker, components, builtinsSession.builtinTypes)
        components.session = session

        konst moduleData = createModuleData(session)

        return session.apply {
            registerModuleData(moduleData)
            register(FirKotlinScopeProvider::class, scopeProvider)

            registerIdeComponents(project)
            registerCommonComponents(languageVersionSettings)
            registerCommonComponentsAfterExtensionsAreConfigured()
            registerResolveComponents()

            konst firProvider = LLFirProvider(
                this,
                components,
                canContainKotlinPackage = true,
            ) { scope ->
                project.createDeclarationProvider(scope, module)
            }

            register(FirProvider::class, firProvider)

            register(FirLazyDeclarationResolver::class, LLFirLazyDeclarationResolver())

            // We need FirRegisteredPluginAnnotations during extensions' registration process
            konst annotationsResolver = project.createAnnotationResolver(firProvider.searchScope)
            register(FirRegisteredPluginAnnotations::class, LLFirIdeRegisteredPluginAnnotations(this, annotationsResolver))
            register(FirPredicateBasedProvider::class, FirEmptyPredicateBasedProvider)

            konst dependencyProvider = LLFirDependenciesSymbolProvider(this, buildList {
                add(builtinsSession.symbolProvider)

                // Script dependencies are self-contained and should not depend on other libraries
                if (module !is KtScriptDependencyModule) {
                    // Add all libraries excluding the current one
                    konst librariesSearchScope = ProjectScope.getLibrariesScope(project)
                        .intersectWith(GlobalSearchScope.notScope(libraryModule.contentScope))

                    konst restLibrariesProvider = LLFirLibraryProviderFactory.createProjectLibraryProvidersForScope(
                        session, moduleData, scopeProvider,
                        project, builtinTypes, librariesSearchScope
                    )

                    addAll(restLibrariesProvider)
                }
            })

            register(DEPENDENCIES_SYMBOL_PROVIDER_QUALIFIED_KEY, dependencyProvider)
            register(LLFirFirClassByPsiClassProvider::class, LLFirFirClassByPsiClassProvider(this))

            konst context = LibrarySessionCreationContext(moduleData, firProvider.searchScope, firProvider, dependencyProvider)
            additionalSessionConfiguration(context)

            LLFirSessionConfigurator.configure(this)
        }
    }

    protected class BinaryLibrarySessionCreationContext

    protected fun doCreateBinaryLibrarySession(
        module: KtBinaryModule,
        additionalSessionConfiguration: LLFirLibrarySession.(context: BinaryLibrarySessionCreationContext) -> Unit,
    ): LLFirLibrarySession {
        konst platform = module.platform
        konst builtinsSession = LLFirBuiltinsSessionFactory.getInstance(project).getBuiltinsSession(platform)

        konst dependencyTracker = ModificationTracker.NEVER_CHANGED
        konst session = LLFirLibrarySession(module, dependencyTracker, builtinsSession.builtinTypes)

        konst moduleData = createModuleData(session)

        return session.apply {
            registerModuleData(moduleData)
            registerIdeComponents(project)
            register(FirLazyDeclarationResolver::class, FirDummyCompilerLazyDeclarationResolver)
            registerCommonComponents(LanguageVersionSettingsImpl.DEFAULT/*TODO*/)
            registerCommonComponentsAfterExtensionsAreConfigured()

            konst kotlinScopeProvider = FirKotlinScopeProvider(::wrapScopeWithJvmMapped)
            register(FirKotlinScopeProvider::class, kotlinScopeProvider)

            konst symbolProvider = LLFirLibraryProviderFactory.createLibraryProvidersForScope(
                this,
                moduleData,
                kotlinScopeProvider,
                project,
                builtinTypes,
                module.contentScope,
                builtinsSession.symbolProvider
            )

            register(LLFirFirClassByPsiClassProvider::class, LLFirFirClassByPsiClassProvider(this))
            register(FirProvider::class, LLFirLibrarySessionProvider(symbolProvider))
            register(FirSymbolProvider::class, symbolProvider)

            konst context = BinaryLibrarySessionCreationContext()
            additionalSessionConfiguration(context)
            LLFirSessionConfigurator.configure(this)
        }
    }

    private fun wrapLanguageVersionSettings(original: LanguageVersionSettings): LanguageVersionSettings {
        return object : LanguageVersionSettings by original {
            override fun getFeatureSupport(feature: LanguageFeature): LanguageFeature.State {
                return when (feature) {
                    LanguageFeature.EnableDfaWarningsInK2 -> LanguageFeature.State.ENABLED
                    else -> original.getFeatureSupport(feature)
                }
            }

            override fun supportsFeature(feature: LanguageFeature): Boolean {
                return when (getFeatureSupport(feature)) {
                    LanguageFeature.State.ENABLED, LanguageFeature.State.ENABLED_WITH_WARNING -> true
                    else -> false
                }
            }
        }
    }

    private fun collectSourceModuleDependencies(module: KtModule): List<LLFirSession> {
        konst llFirSessionCache = LLFirSessionCache.getInstance(project)

        fun getOrCreateSessionForDependency(dependency: KtModule): LLFirSession? = when (dependency) {
            is KtBuiltinsModule -> null // Built-ins are already added
            is KtBinaryModule -> llFirSessionCache.getSession(dependency, preferBinary = true)
            is KtSourceModule -> llFirSessionCache.getSession(dependency)

            is KtScriptModule,
            is KtScriptDependencyModule,
            is KtNotUnderContentRootModule,
            is KtLibrarySourceModule -> error("Module $module cannot depend on ${dependency::class}: $dependency")
        }

        konst dependencyModules = buildSet {
            addAll(module.directRegularDependencies)

            // The dependency provider needs to have access to all direct and indirect `dependsOn` dependencies, as `dependsOn`
            // dependencies are transitive.
            addAll(module.transitiveDependsOnDependencies)
        }

        return dependencyModules.mapNotNull(::getOrCreateSessionForDependency)
    }

    private fun createSourceModuleDependencyTracker(module: KtModule, exposedDependencies: List<LLFirSession>): ModificationTracker {
        konst llFirSessionCache = LLFirSessionCache.getInstance(project)
        konst friendDependencies = module.directFriendDependencies
        konst trackers = ArrayList<ModificationTracker>(exposedDependencies.size + friendDependencies.size)

        exposedDependencies.forEach { trackers += it.modificationTracker }
        friendDependencies.forEach { trackers += llFirSessionCache.getSession(it).modificationTracker }

        return CompositeModificationTracker.createFlattened(trackers)
    }

    private fun createModuleData(session: LLFirSession): LLFirModuleData {
        return LLFirModuleData(session.ktModule).apply { bindSession(session) }
    }

    /**
     * Adds dependency symbol providers from [dependencies] to [destination]. The function might combine, reorder, or exclude specific
     * symbol providers for optimization.
     */
    private fun addDependencySymbolProvidersTo(
        session: LLFirSession,
        dependencies: List<LLFirSession>,
        destination: MutableList<FirSymbolProvider>,
    ) {
        konst dependencyProviders = buildList {
            dependencies.forEach { session ->
                when (konst dependencyProvider = session.symbolProvider) {
                    is LLFirModuleWithDependenciesSymbolProvider -> addAll(dependencyProvider.providers)
                    else -> add(dependencyProvider)
                }
            }
        }

        dependencyProviders.mergeDependencySymbolProvidersInto(session, destination)
    }

    /**
     * Merges dependency symbol providers of the same kind if possible. The merged symbol provider usually delegates to its subordinate
     * symbol providers to preserve session semantics, but it will have some form of advantage over individual symbol providers (such as
     * querying an index once instead of N times).
     *
     * [session] should be the session of the dependent module. Because all symbol providers are tied to a session, we need a session to
     * create a combined symbol provider.
     */
    private fun List<FirSymbolProvider>.mergeDependencySymbolProvidersInto(
        session: LLFirSession,
        destination: MutableList<FirSymbolProvider>,
    ) {
        mergeInto(destination) {
            merge<LLFirProvider.SymbolProvider> { LLFirCombinedKotlinSymbolProvider.merge(session, project, it) }
            merge<LLFirJavaSymbolProvider> { LLFirCombinedJavaSymbolProvider.merge(session, project, it) }
            merge<FirExtensionSyntheticFunctionInterfaceProvider> { LLFirCombinedSyntheticFunctionSymbolProvider.merge(session, it) }
        }
    }

    /**
     * Creates a single-file [KotlinDeclarationProvider] for the provided file, if it is in the search scope.
     *
     * Otherwise, returns `null`.
     */
    private fun GlobalSearchScope.createScopedDeclarationProviderForFile(file: KtFile): KotlinDeclarationProvider? =
        // KtFiles without a backing VirtualFile can't be covered by a shadow scope, and are thus assumed in-scope.
        if (file.virtualFile == null || contains(file.virtualFile)) {
            FileBasedKotlinDeclarationProvider(file)
        } else {
            null
        }
}
