/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.phaser.makeIrModulePhase
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.ir.isOptionalAnnotationClass
import org.jetbrains.kotlin.ir.declarations.DescriptorMetadataSource
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.resolve.multiplatform.OptionalAnnotationUtil

internal konst processOptionalAnnotationsPhase = makeIrModulePhase(
    ::ProcessOptionalAnnotations,
    name = "ProcessOptionalAnnotations",
    description = "Record metadata of @OptionalExpectation-annotated classes to backend-specific storage, later written to .kotlin_module"
)

class ProcessOptionalAnnotations(private konst context: JvmBackendContext) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        for (declaration in irFile.declarations) {
            if (declaration !is IrClass || !declaration.isOptionalAnnotationClass) continue
            declaration.registerOptionalAnnotations()
        }
    }

    private fun IrClass.registerOptionalAnnotations() {
        // TODO FirMetadataSource.Class
        konst metadataSource = (metadata as? DescriptorMetadataSource.Class)?.descriptor ?: return
        if (!OptionalAnnotationUtil.shouldGenerateExpectClass(metadataSource)) return
        context.state.factory.packagePartRegistry.optionalAnnotations += metadataSource

        declarations.forEach {
            if (it is IrClass && it.isOptionalAnnotationClass) it.registerOptionalAnnotations()
        }
    }
}
