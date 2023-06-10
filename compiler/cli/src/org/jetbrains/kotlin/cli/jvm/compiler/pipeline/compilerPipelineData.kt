/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.compiler.pipeline

import org.jetbrains.kotlin.backend.common.actualizer.IrActualizedResult
import org.jetbrains.kotlin.cli.common.GroupedKtSources
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.backend.Fir2IrComponents
import org.jetbrains.kotlin.fir.backend.Fir2IrPluginContext
import org.jetbrains.kotlin.fir.backend.jvm.JvmFir2IrExtensions
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.platform.TargetPlatform

// ---

data class ModuleCompilerInput(
    konst targetId: TargetId,
    konst groupedSources: GroupedKtSources,
    konst commonPlatform: TargetPlatform,
    konst platform: TargetPlatform,
    konst configuration: CompilerConfiguration,
    konst friendFirModules: Collection<FirModuleData> = emptyList()
)

data class ModuleCompilerEnvironment(
    konst projectEnvironment: AbstractProjectEnvironment,
    konst diagnosticsReporter: BaseDiagnosticsCollector
)

data class ModuleCompilerOutput(
    konst generationState: GenerationState
)

data class ModuleCompilerIrBackendInput(
    konst targetId: TargetId,
    konst configuration: CompilerConfiguration,
    konst extensions: JvmFir2IrExtensions,
    konst irModuleFragment: IrModuleFragment,
    konst components: Fir2IrComponents,
    konst pluginContext: Fir2IrPluginContext,
    konst irActualizedResult: IrActualizedResult?
)
