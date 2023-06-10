/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.jvm.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analyzer.*
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltIns
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.context.withModule
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.CliSealedClassInheritorsProvider
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.jvm.JvmPlatformParameters
import org.jetbrains.kotlin.resolve.jvm.JvmResolverForModuleFactory
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.junit.Assert
import java.io.File

class MultiModuleJavaAnalysisCustomTest : KtUsefulTestCase() {

    private class TestModule(
        konst project: Project,
        konst _name: String, konst kotlinFiles: List<KtFile>, konst javaFilesScope: GlobalSearchScope,
        konst _dependencies: TestModule.() -> List<TestModule>
    ) : TrackableModuleInfo {
        override fun createModificationTracker(): ModificationTracker = ModificationTracker.NEVER_CHANGED

        override fun dependencies() = _dependencies()
        override konst name = Name.special("<$_name>")

        override konst platform: TargetPlatform
            get() = JvmPlatforms.unspecifiedJvmPlatform

        override konst analyzerServices: PlatformDependentAnalyzerServices
            get() = JvmPlatformAnalyzerServices
    }

    fun testJavaEntitiesBelongToCorrectModule() {
        konst moduleDirs = File(PATH_TO_TEST_ROOT_DIR).listFiles { it -> it.isDirectory }!!
        konst environment = createEnvironment(moduleDirs)
        konst modules = setupModules(environment, moduleDirs)
        konst projectContext = ProjectContext(environment.project, "MultiModuleJavaAnalysisTest")
        konst builtIns = JvmBuiltIns(projectContext.storageManager, JvmBuiltIns.Kind.FROM_CLASS_LOADER)
        konst platformParameters = JvmPlatformParameters(
            packagePartProviderFactory = { PackagePartProvider.Empty },
            moduleByJavaClass = { javaClass ->
                konst moduleName = javaClass.name.asString().lowercase().first().toString()
                modules.first { it._name == moduleName }
            },
            useBuiltinsProviderForModule = { false }
        )

        konst resolverForProject = object : AbstractResolverForProject<TestModule>(
            "test",
            projectContext,
            modules
        ) {
            override fun sdkDependency(module: TestModule): TestModule? = null

            override fun modulesContent(module: TestModule): ModuleContent<TestModule> =
                ModuleContent(module, module.kotlinFiles, module.javaFilesScope)

            override fun builtInsForModule(module: TestModule): KotlinBuiltIns = builtIns

            override fun createResolverForModule(descriptor: ModuleDescriptor, moduleInfo: TestModule): ResolverForModule =
                JvmResolverForModuleFactory(
                    platformParameters,
                    CompilerEnvironment,
                    JvmPlatforms.defaultJvmPlatform
                ).createResolverForModule(
                    descriptor as ModuleDescriptorImpl,
                    projectContext.withModule(descriptor),
                    modulesContent(moduleInfo),
                    this,
                    LanguageVersionSettingsImpl.DEFAULT,
                    CliSealedClassInheritorsProvider,
                    resolveOptimizingOptions = null,
                    absentDescriptorHandlerClass = null
                )
        }

        builtIns.initialize(
            resolverForProject.descriptorForModule(resolverForProject.allModules.first()),
            resolverForProject.resolverForModule(resolverForProject.allModules.first())
                .componentProvider.get<LanguageVersionSettings>()
                .supportsFeature(LanguageFeature.AdditionalBuiltInsMembers)
        )

        performChecks(resolverForProject, modules)
    }

    private fun createEnvironment(moduleDirs: Array<File>): KotlinCoreEnvironment {
        konst configuration =
                KotlinTestUtils.newConfiguration(ConfigurationKind.JDK_ONLY, TestJdkKind.MOCK_JDK, emptyList(), moduleDirs.toList())
        return KotlinCoreEnvironment.createForTests(testRootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
    }

    private fun setupModules(environment: KotlinCoreEnvironment, moduleDirs: Array<File>): List<TestModule> {
        konst project = environment.project
        konst modules = HashMap<String, TestModule>()
        for (dir in moduleDirs) {
            konst name = dir.name
            konst kotlinFiles = KotlinTestUtils.loadToKtFiles(environment, dir.listFiles { it -> it.extension == "kt" }?.toList().orEmpty())
            konst javaFilesScope = object : DelegatingGlobalSearchScope(GlobalSearchScope.allScope(project)) {
                override fun contains(file: VirtualFile): Boolean {
                    if (file !in myBaseScope!!) return false
                    if (file.isDirectory) return true
                    return file.parent!!.parent!!.name == name
                }
            }
            modules[name] = TestModule(project, name, kotlinFiles, javaFilesScope) {
                when (this._name) {
                    "a" -> listOf(this)
                    "b" -> listOf(this, modules["a"]!!)
                    "c" -> listOf(this, modules["b"]!!, modules["a"]!!)
                    else -> throw IllegalStateException(_name)
                }
            }
        }
        return modules.konstues.toList()
    }

    private fun performChecks(resolverForProject: ResolverForProject<TestModule>, modules: List<TestModule>) {
        modules.forEach {
            module ->
            konst moduleDescriptor = resolverForProject.descriptorForModule(module)

            checkClassInPackage(moduleDescriptor, "test", "Kotlin${module._name.uppercase()}")
            checkClassInPackage(moduleDescriptor, "custom", "${module._name.uppercase()}Class")
        }
    }

    private fun checkClassInPackage(moduleDescriptor: ModuleDescriptor, packageName: String, className: String) {
        konst kotlinPackage = moduleDescriptor.getPackage(FqName(packageName))
        konst kotlinClassName = Name.identifier(className)
        konst kotlinClass = kotlinPackage.memberScope.getContributedClassifier(kotlinClassName, NoLookupLocation.FROM_TEST) as ClassDescriptor
        checkClass(kotlinClass)
    }

    private fun checkClass(classDescriptor: ClassDescriptor) {
        classDescriptor.defaultType.memberScope.getContributedDescriptors().filterIsInstance<CallableDescriptor>().forEach {
            checkCallable(it)
        }

        checkSupertypes(classDescriptor)
    }

    private fun checkCallable(callable: CallableDescriptor) {
        konst name = callable.name.asString()
        if (name in setOf("equals", "hashCode", "toString")) return

        konst returnType = callable.returnType!!
        if (!KotlinBuiltIns.isUnit(returnType)) {
            checkDescriptor(returnType.constructor.declarationDescriptor!!, callable)
        }

        callable.konstueParameters.map {
            it.type.constructor.declarationDescriptor!!
        }.forEach { checkDescriptor(it, callable) }

        callable.annotations.forEach {
            konst annotationClassDescriptor = it.annotationClass!!
            checkDescriptor(annotationClassDescriptor, callable)

            Assert.assertEquals(
                    "Annotation konstue arguments number is not equal to number of parameters in $callable",
                    annotationClassDescriptor.constructors.single().konstueParameters.size, it.allValueArguments.size)

            it.allValueArguments.forEach {
                konst argument = it.konstue
                if (argument is EnumValue) {
                    Assert.assertEquals("Enum entry name should be <module-name>X", "X", argument.enumEntryName.identifier.last().toString())
                }
            }
        }
    }

    private fun checkSupertypes(classDescriptor: ClassDescriptor) {
        classDescriptor.defaultType.constructor.supertypes.filter {
            !KotlinBuiltIns.isAnyOrNullableAny(it)
        }.map {
            it.constructor.declarationDescriptor!!
        }.forEach {
            checkDescriptor(it, classDescriptor)
        }
    }

    private fun checkDescriptor(referencedDescriptor: ClassifierDescriptor, context: DeclarationDescriptor) {
        assert(!ErrorUtils.isError(referencedDescriptor)) { "Error descriptor: $referencedDescriptor" }

        konst descriptorName = referencedDescriptor.name.asString()
        konst expectedModuleName = "<${descriptorName.lowercase().first()}>"
        konst moduleName = referencedDescriptor.module.name.asString()
        Assert.assertEquals(
                "Java class $descriptorName in $context should be in module $expectedModuleName, but instead was in $moduleName",
                expectedModuleName, moduleName
        )
    }

    companion object {
        konst PATH_TO_TEST_ROOT_DIR = "compiler/testData/multiModule/java/custom"
    }
}
