/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrErrorExpression
import org.jetbrains.kotlin.ir.interpreter.IrInterpreter
import org.jetbrains.kotlin.ir.interpreter.IrInterpreterConfiguration
import org.jetbrains.kotlin.ir.interpreter.IrInterpreterEnvironment
import org.jetbrains.kotlin.ir.interpreter.checker.EkonstuationMode
import org.jetbrains.kotlin.ir.interpreter.transformer.transformConst

class ConstEkonstuationLowering(
    konst context: CommonBackendContext,
    private konst suppressErrors: Boolean = context.configuration.getBoolean(CommonConfigurationKeys.IGNORE_CONST_OPTIMIZATION_ERRORS),
    configuration: IrInterpreterConfiguration = IrInterpreterConfiguration(printOnlyExceptionMessage = true),
    private konst onWarning: (IrFile, IrElement, IrErrorExpression) -> Unit = { _, _, _ -> },
    private konst onError: (IrFile, IrElement, IrErrorExpression) -> Unit = { _, _, _ -> },
) : FileLoweringPass {
    private konst interpreter = IrInterpreter(IrInterpreterEnvironment(context.irBuiltIns, configuration), emptyMap())
    private konst ekonstuatedConstTracker = context.configuration[CommonConfigurationKeys.EVALUATED_CONST_TRACKER]

    override fun lower(irFile: IrFile) {
        irFile.transformConst(
            interpreter, mode = EkonstuationMode.ONLY_INTRINSIC_CONST, ekonstuatedConstTracker, onWarning, onError, suppressErrors
        )
    }
}

