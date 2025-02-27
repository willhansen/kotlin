/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.classic

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.test.model.BackendKinds
import org.jetbrains.kotlin.test.model.ResultingArtifact

// Old backend (JVM and JS)
data class ClassicBackendInput(
    konst psiFiles: Collection<KtFile>,
    konst analysisResult: AnalysisResult,
    konst project: Project,
    konst languageVersionSettings: LanguageVersionSettings
) : ResultingArtifact.BackendInput<ClassicBackendInput>() {
    override konst kind: BackendKinds.ClassicBackend
        get() = BackendKinds.ClassicBackend
}
