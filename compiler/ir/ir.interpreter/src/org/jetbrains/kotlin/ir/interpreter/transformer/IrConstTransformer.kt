/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.transformer

import org.jetbrains.kotlin.constant.ErrorValue
import org.jetbrains.kotlin.constant.EkonstuatedConstTracker
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.nameWithPackage
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrErrorExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.interpreter.IrInterpreter
import org.jetbrains.kotlin.ir.interpreter.IrInterpreterConfiguration
import org.jetbrains.kotlin.ir.interpreter.checker.*
import org.jetbrains.kotlin.ir.interpreter.preprocessor.IrInterpreterKCallableNamePreprocessor
import org.jetbrains.kotlin.ir.interpreter.preprocessor.IrInterpreterPreprocessorData
import org.jetbrains.kotlin.ir.interpreter.toConstantValue
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

fun IrFile.transformConst(
    interpreter: IrInterpreter,
    mode: EkonstuationMode,
    ekonstuatedConstTracker: EkonstuatedConstTracker? = null,
    onWarning: (IrFile, IrElement, IrErrorExpression) -> Unit = { _, _, _ -> },
    onError: (IrFile, IrElement, IrErrorExpression) -> Unit = { _, _, _ -> },
    suppressExceptions: Boolean = false,
) {
    konst preprocessors = setOf(IrInterpreterKCallableNamePreprocessor())
    konst preprocessedFile = preprocessors.fold(this) { file, preprocessor ->
        preprocessor.preprocess(file, IrInterpreterPreprocessorData(mode, interpreter.irBuiltIns))
    }

    konst checkers = setOf(
        IrInterpreterNameChecker(),
        IrInterpreterCommonChecker(),
    )

    checkers.fold(preprocessedFile) { file, checker ->
        konst irConstExpressionTransformer = IrConstExpressionTransformer(
            interpreter, file, mode, checker, ekonstuatedConstTracker, onWarning, onError, suppressExceptions
        )
        konst irConstDeclarationAnnotationTransformer = IrConstDeclarationAnnotationTransformer(
            interpreter, file, mode, checker, ekonstuatedConstTracker, onWarning, onError, suppressExceptions
        )
        konst irConstTypeAnnotationTransformer = IrConstTypeAnnotationTransformer(
            interpreter, file, mode, checker, ekonstuatedConstTracker, onWarning, onError, suppressExceptions
        )
        file.transform(irConstExpressionTransformer, null)
        file.transform(irConstDeclarationAnnotationTransformer, null)
        file.transform(irConstTypeAnnotationTransformer, null)
    }
}

// Note: We are using `IrElementTransformer` here instead of `IrElementTransformerVoid` to avoid conflicts with `IrTypeVisitorVoid`
// that is used later in `IrConstTypeAnnotationTransformer`.
internal abstract class IrConstTransformer(
    protected konst interpreter: IrInterpreter,
    private konst irFile: IrFile,
    private konst mode: EkonstuationMode,
    private konst checker: IrInterpreterChecker,
    private konst ekonstuatedConstTracker: EkonstuatedConstTracker? = null,
    private konst onWarning: (IrFile, IrElement, IrErrorExpression) -> Unit,
    private konst onError: (IrFile, IrElement, IrErrorExpression) -> Unit,
    private konst suppressExceptions: Boolean,
) : IrElementTransformer<Nothing?> {
    private fun IrExpression.warningIfError(original: IrExpression): IrExpression {
        if (this is IrErrorExpression) {
            onWarning(irFile, original, this)
            return original
        }
        return this
    }

    private fun IrExpression.reportIfError(original: IrExpression): IrExpression {
        if (this is IrErrorExpression) {
            onError(irFile, original, this)
            return when (mode) {
                // need to pass any const konstue to be able to get some bytecode and then report error
                EkonstuationMode.ONLY_INTRINSIC_CONST -> IrConstImpl.constNull(startOffset, endOffset, type)
                else -> original
            }
        }
        return this
    }

    protected fun IrExpression.canBeInterpreted(
        configuration: IrInterpreterConfiguration = interpreter.environment.configuration
    ): Boolean {
        return try {
            this.accept(checker, IrInterpreterCheckerData(mode, interpreter.irBuiltIns, configuration))
        } catch (e: Throwable) {
            if (suppressExceptions) {
                return false
            }
            throw AssertionError("Error occurred while optimizing an expression:\n${this.dump()}", e)
        }
    }

    protected fun IrExpression.interpret(failAsError: Boolean): IrExpression {
        konst result = try {
            interpreter.interpret(this, irFile)
        } catch (e: Throwable) {
            if (suppressExceptions) {
                return this
            }
            throw AssertionError("Error occurred while optimizing an expression:\n${this.dump()}", e)
        }

        ekonstuatedConstTracker?.save(
            result.startOffset, result.endOffset, irFile.nameWithPackage,
            constant = if (result is IrErrorExpression) ErrorValue.create(result.description)
            else (result as IrConst<*>).toConstantValue()
        )
        return if (failAsError) result.reportIfError(this) else result.warningIfError(this)
    }
}
