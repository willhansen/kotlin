/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.configurators

import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.lifetime.KtDefaultLifetimeTokenProvider
import org.jetbrains.kotlin.analysis.api.lifetime.KtReadActionConfinementDefaultLifetimeTokenProvider
import org.jetbrains.kotlin.analysis.api.standalone.base.project.structure.StandaloneProjectFactory
import org.jetbrains.kotlin.analysis.decompiled.light.classes.ClsJavaStubByVirtualFileCache
import org.jetbrains.kotlin.analysis.decompiled.light.classes.DecompiledLightClassesFactory
import org.jetbrains.kotlin.analysis.decompiler.psi.BuiltInDefinitionFile
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtClsFile
import org.jetbrains.kotlin.analysis.project.structure.KtModuleScopeProvider
import org.jetbrains.kotlin.analysis.project.structure.KtModuleScopeProviderImpl
import org.jetbrains.kotlin.analysis.providers.*
import org.jetbrains.kotlin.analysis.providers.impl.*
import org.jetbrains.kotlin.analysis.test.framework.project.structure.ktModuleProvider
import org.jetbrains.kotlin.analysis.test.framework.services.environmentManager
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestServiceRegistrar
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFileClassProvider
import org.jetbrains.kotlin.test.services.TestServices

object AnalysisApiBaseTestServiceRegistrar: AnalysisApiTestServiceRegistrar()  {
    override fun registerProjectExtensionPoints(project: MockProject, testServices: TestServices) {}

    @OptIn(KtAnalysisApiInternals::class)
    override fun registerProjectServices(project: MockProject, testServices: TestServices) {
        project.apply {
            registerService(KotlinModificationTrackerFactory::class.java, KotlinStaticModificationTrackerFactory::class.java)
            registerService(KtDefaultLifetimeTokenProvider::class.java, KtReadActionConfinementDefaultLifetimeTokenProvider::class.java)

            //KotlinClassFileDecompiler is registered as application service so it's available for the tests run in parallel as well
            //when the decompiler is registered, for compiled class KtClsFile is created instead of ClsFileImpl
            //and KtFile doesn't return any classes in classOwner.getClasses if there is no KtFileClassProvider
            //but getClasses is used during java resolve, thus it's required to return some PsiClass for such cases
            registerService(KtFileClassProvider::class.java, KtClsFileClassProvider(project))
            registerService(ClsJavaStubByVirtualFileCache::class.java, ClsJavaStubByVirtualFileCache())
        }
    }

    class KtClsFileClassProvider(konst project: Project) : KtFileClassProvider {
        override fun getFileClasses(file: KtFile): Array<PsiClass> {
            konst virtualFile = file.virtualFile
            konst classOrObject = file.declarations.filterIsInstance<KtClassOrObject>().singleOrNull()
            if (file is KtClsFile && virtualFile != null) {
                DecompiledLightClassesFactory.createClsJavaClassFromVirtualFile(file, virtualFile, classOrObject, project)?.let {
                    return arrayOf(it)
                }
            }
            return PsiClass.EMPTY_ARRAY
        }
    }

    override fun registerProjectModelServices(project: MockProject, testServices: TestServices) {
        konst moduleStructure = testServices.ktModuleProvider.getModuleStructure()
        konst allKtFiles = moduleStructure.mainModules.flatMap { it.files.filterIsInstance<KtFile>() }
        konst roots = StandaloneProjectFactory.getVirtualFilesForLibraryRoots(
            moduleStructure.binaryModules.flatMap { binary -> binary.getBinaryRoots() },
            testServices.environmentManager.getProjectEnvironment()
        ).distinct()
        project.apply {
            registerService(KtModuleScopeProvider::class.java, KtModuleScopeProviderImpl())
            registerService(KotlinAnnotationsResolverFactory::class.java, KotlinStaticAnnotationsResolverFactory(allKtFiles))

            konst filter = BuiltInDefinitionFile.FILTER_OUT_CLASSES_EXISTING_AS_JVM_CLASS_FILES
            try {
                BuiltInDefinitionFile.FILTER_OUT_CLASSES_EXISTING_AS_JVM_CLASS_FILES = false
                registerService(
                    KotlinDeclarationProviderFactory::class.java, KotlinStaticDeclarationProviderFactory(
                        project,
                        allKtFiles,
                        additionalRoots = roots
                    )
                )
            } finally {
                BuiltInDefinitionFile.FILTER_OUT_CLASSES_EXISTING_AS_JVM_CLASS_FILES = filter
            }
            registerService(KotlinDeclarationProviderMerger::class.java, KotlinStaticDeclarationProviderMerger(project))
            registerService(KotlinPackageProviderFactory::class.java, KotlinStaticPackageProviderFactory(project, allKtFiles))
            registerService(KotlinResolutionScopeProvider::class.java, KotlinByModulesResolutionScopeProvider::class.java)
        }
    }

    override fun registerApplicationServices(application: MockApplication, testServices: TestServices) {
    }
}
