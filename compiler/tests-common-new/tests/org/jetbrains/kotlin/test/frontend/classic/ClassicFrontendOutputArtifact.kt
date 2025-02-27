/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.classic

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.model.ResultingArtifact
import org.jetbrains.kotlin.test.model.TestFile

data class ClassicFrontendOutputArtifact(
    konst allKtFiles: Map<TestFile, KtFile>,
    konst analysisResult: AnalysisResult,
    konst project: Project,
    konst languageVersionSettings: LanguageVersionSettings
) : ResultingArtifact.FrontendOutput<ClassicFrontendOutputArtifact>() {
    override konst kind: FrontendKinds.ClassicFrontend
        get() = FrontendKinds.ClassicFrontend

    konst ktFiles: Map<TestFile, KtFile> = allKtFiles.filterKeys { !it.isAdditional }
}
