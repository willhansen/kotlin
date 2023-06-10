/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.ir

import org.jetbrains.kotlin.backend.common.actualizer.IrActualizer
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.compilerConfigurationProvider

class ActualizerOnlyFacade(
    konst testServices: TestServices,
) : AbstractTestFacade<IrBackendInput, IrBackendInput>() {
    override fun transform(module: TestModule, inputArtifact: IrBackendInput): IrBackendInput {
        if (module.useIrActualizer()) {
            IrActualizer.actualize(
                inputArtifact.irModuleFragment,
                inputArtifact.dependentIrModuleFragments,
                inputArtifact.diagnosticReporter,
                testServices.compilerConfigurationProvider.getCompilerConfiguration(module).languageVersionSettings
            )
        }
        return inputArtifact
    }

    private fun TestModule.useIrActualizer(): Boolean {
        return frontendKind == FrontendKinds.FIR && languageVersionSettings.supportsFeature(LanguageFeature.MultiPlatformProjects)
    }

    override konst inputKind: TestArtifactKind<IrBackendInput> = BackendKinds.IrBackend
    override konst outputKind: TestArtifactKind<IrBackendInput> = BackendKinds.IrBackend

    override fun shouldRunAnalysis(module: TestModule): Boolean = true
}