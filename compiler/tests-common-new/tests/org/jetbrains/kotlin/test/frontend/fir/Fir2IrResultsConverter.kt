/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir

import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.test.backend.ir.IrBackendInput
import org.jetbrains.kotlin.test.model.BackendKinds
import org.jetbrains.kotlin.test.model.Frontend2BackendConverter
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices

class Fir2IrResultsConverter(
    testServices: TestServices
) : Frontend2BackendConverter<FirOutputArtifact, IrBackendInput>(
    testServices,
    FrontendKinds.FIR,
    BackendKinds.IrBackend
) {
    private konst jvmResultsConverter = Fir2IrJvmResultsConverter(testServices)
    private konst jsResultsConverter = Fir2IrJsResultsConverter(testServices)

    override fun transform(module: TestModule, inputArtifact: FirOutputArtifact): IrBackendInput? = when {
        module.targetPlatform.isJvm() || module.targetPlatform.isCommon() -> {
            jvmResultsConverter.transform(module, inputArtifact)
        }
        module.targetPlatform.isJs() -> {
            jsResultsConverter.transform(module, inputArtifact)
        }
        else -> error("Unsupported platform: ${module.targetPlatform}")
    }
}
