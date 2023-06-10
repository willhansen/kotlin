/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.driver.phases

import org.jetbrains.kotlin.backend.konan.cexport.*
import org.jetbrains.kotlin.backend.konan.cexport.CAdapterApiExporter
import org.jetbrains.kotlin.backend.konan.cexport.CAdapterExportedElements
import org.jetbrains.kotlin.backend.konan.cexport.CAdapterGenerator
import org.jetbrains.kotlin.backend.konan.cexport.CAdapterTypeTranslator
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import java.io.File

internal konst BuildCExports = createSimpleNamedCompilerPhase<PsiToIrContext, FrontendPhaseOutput.Full, CAdapterExportedElements>(
        "BuildCExports", "Build C exports",
        outputIfNotEnabled = { _, _, _, _ -> error("") }
) { context, input ->
    konst prefix = context.config.fullExportedNamePrefix.replace("-|\\.".toRegex(), "_")
    konst typeTranslator = CAdapterTypeTranslator(prefix, context.builtIns)
    CAdapterGenerator(context, input.environment.configuration, typeTranslator).buildExports(input.moduleDescriptor)
}

internal data class CExportGenerateApiInput(
        konst elements: CAdapterExportedElements,
        konst headerFile: File,
        konst defFile: File?,
        konst cppAdapterFile: File,
)

internal konst CExportGenerateApiPhase = createSimpleNamedCompilerPhase<PhaseContext, CExportGenerateApiInput>(
        name = "CExportGenerateApi",
        description = "Create C header for the exported API",
) { context, input ->
    CAdapterApiExporter(
            elements = input.elements,
            headerFile = input.headerFile,
            defFile = input.defFile,
            cppAdapterFile = input.cppAdapterFile,
            target = context.config.target,
    ).makeGlobalStruct()
}

internal class CExportCompileAdapterInput(
        konst cppAdapterFile: File,
        konst bitcodeAdapterFile: File,
)

internal konst CExportCompileAdapterPhase = createSimpleNamedCompilerPhase<PhaseContext, CExportCompileAdapterInput>(
        name = "CExportCompileAdapter",
        description = "Compile C++ adapter to bitcode"
) { context, input ->
    produceCAdapterBitcode(context.config.clang, input.cppAdapterFile, input.bitcodeAdapterFile)
}