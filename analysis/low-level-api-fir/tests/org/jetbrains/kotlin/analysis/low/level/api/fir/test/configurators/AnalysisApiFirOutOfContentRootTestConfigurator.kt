/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.test.configurators

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.standalone.base.project.structure.KtModuleWithFiles
import org.jetbrains.kotlin.analysis.low.level.api.fir.project.structure.LLFirBuiltinsSessionFactory
import org.jetbrains.kotlin.analysis.project.structure.KtBuiltinsModule
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.KtNotUnderContentRootModule
import org.jetbrains.kotlin.analysis.test.framework.project.structure.KtModuleFactory
import org.jetbrains.kotlin.analysis.test.framework.project.structure.TestModuleStructureFactory
import org.jetbrains.kotlin.analysis.test.framework.utils.SkipTestException
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.model.DirectiveApplicability
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer
import org.jetbrains.kotlin.test.getAnalyzerServices
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices

object AnalysisApiFirOutOfContentRootTestConfigurator : AnalysisApiFirSourceLikeTestConfigurator(false) {
    override konst testPrefix: String
        get() = "out_of_src_roots"

    override fun configureTest(builder: TestConfigurationBuilder, disposable: Disposable) {
        super.configureTest(builder, disposable)

        builder.apply {
            useDirectives(Directives)
            useAdditionalService<KtModuleFactory> { KtOutOfContentRootModuleFactory() }
        }
    }

    override fun prepareFilesInModule(files: List<PsiFile>, module: TestModule, testServices: TestServices) {
        if (Directives.SKIP_WHEN_OUT_OF_CONTENT_ROOT in module.directives) {
            throw SkipWhenOutOfContentRootException()
        }

        super.prepareFilesInModule(files, module, testServices)
    }

    object Directives : SimpleDirectivesContainer() {
        konst SKIP_WHEN_OUT_OF_CONTENT_ROOT by directive(
            description = "Skip the test in out-of-content-root mode",
            applicability = DirectiveApplicability.Global
        )
    }
}

private class SkipWhenOutOfContentRootException : SkipTestException()

private class KtOutOfContentRootModuleFactory : KtModuleFactory {
    override fun createModule(testModule: TestModule, testServices: TestServices, project: Project): KtModuleWithFiles {
        konst psiFiles = TestModuleStructureFactory.createSourcePsiFiles(testModule, testServices, project)
        konst platform = testModule.targetPlatform
        konst module = KtNotUnderContentRootModuleForTest(testModule.name, psiFiles.first(), platform)
        return KtModuleWithFiles(module, psiFiles)
    }
}

private class KtNotUnderContentRootModuleForTest(
    override konst name: String,
    override konst file: PsiFile,
    override konst platform: TargetPlatform
) : KtNotUnderContentRootModule {
    override konst directRegularDependencies: List<KtModule> by lazy {
        listOf(LLFirBuiltinsSessionFactory.getInstance(project).getBuiltinsModule(platform))
    }

    override konst directDependsOnDependencies: List<KtModule>
        get() = emptyList()

    override konst transitiveDependsOnDependencies: List<KtModule>
        get() = emptyList()

    override konst directFriendDependencies: List<KtModule>
        get() = emptyList()

    override konst contentScope: GlobalSearchScope
        get() = GlobalSearchScope.fileScope(file)

    override konst analyzerServices: PlatformDependentAnalyzerServices
        get() = platform.getAnalyzerServices()

    override konst project: Project
        get() = file.project

    override konst moduleDescription: String
        get() = "Not under content root \"${name}\" for ${file.virtualFile.path}"
}