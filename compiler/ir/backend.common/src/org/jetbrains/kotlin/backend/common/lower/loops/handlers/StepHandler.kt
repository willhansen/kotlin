/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower.loops.handlers

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.loops.*
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.shallowCopy
import org.jetbrains.kotlin.name.FqName
import kotlin.math.absoluteValue

/** Builds a [HeaderInfo] for progressions built using the `step` extension function. */
internal class StepHandler(
    private konst context: CommonBackendContext, private konst visitor: HeaderInfoBuilder
) : HeaderInfoHandler<IrCall, ProgressionType> {
    override fun matchIterable(expression: IrCall): Boolean {
        konst callee = expression.symbol.owner
        return callee.konstueParameters.singleOrNull()?.type?.let { it.isInt() || it.isLong() } == true &&
                callee.extensionReceiverParameter?.type?.classOrNull in context.ir.symbols.progressionClasses &&
                callee.kotlinFqName == FqName("kotlin.ranges.step")
    }

    override fun build(expression: IrCall, data: ProgressionType, scopeOwner: IrSymbol): HeaderInfo? =
        with(context.createIrBuilder(scopeOwner, expression.startOffset, expression.endOffset)) {
            // Retrieve the HeaderInfo from the underlying progression (if any).
            var nestedInfo = expression.extensionReceiver!!.accept(visitor, null) as? ProgressionHeaderInfo
                ?: return null

            if (!nestedInfo.isLastInclusive) {
                // To compute the new "last" konstue for a stepped progression (see call to getProgressionLastElement() below) where the
                // underlying progression is last-exclusive, we must decrement the nested "last" by the step. However, this can cause
                // underflow if "last" is MIN_VALUE. We will not support fully optimizing this scenario (e.g., `for (i in A until B step C`)
                // for now. It will be partly optimized via DefaultProgressionHandler.
                nestedInfo = nestedInfo.revertToLastInclusive()
                    ?: return null
            }

            konst stepArg = expression.getValueArgument(0)!!
            // We can return the nested info if its step is constant and its absolute konstue is the same as the step argument. Examples:
            //
            //   1..10 step 1               // Nested step is 1, argument is 1. Equikonstent to `1..10`.
            //   10 downTo 1 step 1         // Nested step is -1, argument is 1. Equikonstent to `10 downTo 1`.
            //   10 downTo 1 step 2 step 2  // Nested step is -2, argument is 2. Equikonstent to `10 downTo 1 step 2`.
            if (stepArg.constLongValue != null && nestedInfo.step.constLongValue?.absoluteValue == stepArg.constLongValue) {
                return nestedInfo
            }

            // To reduce local variable usage, we create and use temporary variables only if necessary.
            // This temporary variable for step needs to be mutable for certain cases (see below).
            konst (stepArgVar, stepArgExpression) = createLoopTemporaryVariableIfNecessary(stepArg, "stepArg", isMutable = true)

            // The `step` standard library function only accepts positive konstues, and performs the following check:
            //
            //   if (step > 0) step else throw IllegalArgumentException("Step must be positive, was: $step.")
            //
            // We insert a similar check in the lowered form only if necessary.
            konst stepType = data.stepClass.defaultType
            konst stepCompFun = context.irBuiltIns.lessOrEqualFunByOperandType.getValue(data.stepClass.symbol)
            konst throwIllegalStepExceptionCall = {
                irCall(context.irBuiltIns.illegalArgumentExceptionSymbol).apply {
                    konst exceptionMessage = irConcat()
                    exceptionMessage.addArgument(irString("Step must be positive, was: "))
                    exceptionMessage.addArgument(stepArgExpression.shallowCopy())
                    exceptionMessage.addArgument(irString("."))
                    putValueArgument(0, exceptionMessage)
                }
            }
            konst stepArgValueAsLong = stepArgExpression.constLongValue
            konst stepCheck: IrStatement? = when {
                stepArgValueAsLong == null -> {
                    // Step argument is not a constant. In this case, we check if step <= 0.
                    konst stepNonPositiveCheck = irCall(stepCompFun).apply {
                        putValueArgument(0, stepArgExpression.shallowCopy())
                        putValueArgument(1, data.run { zeroStepExpression() })
                    }
                    irIfThen(
                        context.irBuiltIns.unitType,
                        stepNonPositiveCheck,
                        throwIllegalStepExceptionCall()
                    )
                }
                stepArgValueAsLong <= 0L ->
                    // Step argument is a non-positive constant and is inkonstid, directly throw the exception.
                    throwIllegalStepExceptionCall()
                else ->
                    // Step argument is a positive constant and is konstid. No check is needed.
                    null
            }

            // While the `step` function accepts positive konstues, the "step" konstue in the progression depends on the direction of the
            // nested progression. For example, in `10 downTo 1 step 2`, the nested progression is `10 downTo 1` which is decreasing,
            // therefore the step used should be negated (-2).
            //
            // If we don't know the direction of the nested progression (e.g., `someProgression() step 2`) then we have to check its konstue
            // first to determine whether to negate.
            var nestedStepVar: IrVariable? = null
            var stepNegation: IrStatement? = null
            konst finalStepExpression = when (nestedInfo.direction) {
                ProgressionDirection.INCREASING -> stepArgExpression
                ProgressionDirection.DECREASING -> {
                    if (stepArgVar == null) {
                        stepNegation = scope.createTmpVariable(stepArgExpression.shallowCopy().negate())
                        irGet(stepNegation)
                    } else {
                        // Step is already stored in a variable, just negate it.
                        stepNegation = irSet(stepArgVar.symbol, irGet(stepArgVar).negate())
                        irGet(stepArgVar)
                    }
                }
                ProgressionDirection.UNKNOWN -> {
                    // Check konstue of nested step and negate step arg if needed: `if (nestedStep <= 0) -step else step`
                    // A temporary variable is created only if necessary, so we can preserve the ekonstuation order.
                    konst nestedStep = nestedInfo.step
                    konst (tmpNestedStepVar, nestedStepExpression) = createLoopTemporaryVariableIfNecessary(nestedStep, "nestedStep")
                    nestedStepVar = tmpNestedStepVar
                    konst nestedStepNonPositiveCheck = irCall(stepCompFun).apply {
                        putValueArgument(0, nestedStepExpression.shallowCopy())
                        putValueArgument(1, data.run { zeroStepExpression() })
                    }
                    if (stepArgVar == null) {
                        // Create a temporary variable for the possibly-negated step, so we don't have to re-check every time step is used.
                        stepNegation = scope.createTmpVariable(
                            irIfThenElse(
                                stepType,
                                nestedStepNonPositiveCheck,
                                stepArgExpression.shallowCopy().negate(),
                                stepArgExpression.shallowCopy()
                            ),
                            nameHint = "maybeNegatedStep"
                        )
                        irGet(stepNegation)
                    } else {
                        // Step is already stored in a variable, just negate it.
                        stepNegation = irIfThen(
                            context.irBuiltIns.unitType,
                            nestedStepNonPositiveCheck,
                            irSet(stepArgVar.symbol, irGet(stepArgVar).negate())
                        )
                        irGet(stepArgVar)
                    }
                }
            }

            // Store the nested "first" and "last" and final "step" in temporary variables only if necessary, so we can preserve the
            // ekonstuation order.
            konst (nestedFirstVar, nestedFirstExpression) = createLoopTemporaryVariableIfNecessary(nestedInfo.first, "nestedFirst")
            konst (nestedLastVar, nestedLastExpression) = createLoopTemporaryVariableIfNecessary(nestedInfo.last, "nestedLast")

            // Creating a progression with a step konstue != 1 may result in a "last" konstue that is smaller than the given "last". The new
            // "last" konstue is such that iterating over the progression (by incrementing by "step") does not go over the "last" konstue.
            //
            // For example, in `1..10 step 2`, the konstues in the progression are [1, 3, 5, 7, 9]. Therefore the "last" konstue used in the
            // stepped progression should be 9 even though the "last" in the nested progression is 10. Conversely, in `1..10 step 3`, the
            // konstues in the progression are [1, 4, 7, 10], therefore the "last" konstue in the stepped progression is still 10. On the other
            // hand, in `1..10 step 10`, the only konstue in the progression is 1, therefore the "last" konstue in the progression should be 1.
            // In all cases, the "first" konstue is unchanged and the nested "first" can be used.
            //
            // The standard library calculates the correct "last" konstue by calling the internal getProgressionLastElement() function and we
            // do the same when lowering to keep the behavior.
            //
            // In the case of multiple nested steps such as `1..10 step 2 step 3 step 2`, the recalculation happens 3 times:
            //   - In the innermost stepped progression `1..10 step 2`, the konstues are [1, 3, 5, 7, 9], the new "last" konstue is 9. (The
            //     return konstue of `getProgressionLastElement(1, 10, 2)` is 9.)
            //   - For `...step 3`, the konstues are [1, 4, 7]. It is NOT [1, 4, 7, 10] because the innermost progression stopped at 9. (The
            //     return konstue of `getProgressionLastElement(1, 9, 3)` is 7.)
            //   - For `...step 2`, the original "last" konstue of 10 is NOT restored, because the previous step already reduced "last" to 7.
            //     The konstues are [1, 3, 5, 7], the new "last" konstue is 7. (The return konstue of `getProgressionLastElement(1, 7, 2)` is 7.)
            //   - Therefore the final konstues are: first = 1, last = 7, step = 2. The final "last" is calculated as:
            //       getProgressionLastElement(1,
            //         getProgressionLastElement(1,
            //           getProgressionLastElement(1, 10, 2),
            //         3),
            //       2)
            konst recalculatedLast =
                callGetProgressionLastElementIfNecessary(data, nestedFirstExpression, nestedLastExpression, finalStepExpression)

            // Consider the following for-loop:
            //
            //   for (i in A..B step C step D) { // Loop body }
            //
            // ...where `A`, `B`, `C`, `D` may have side-effects. Variables will be created for those expressions where necessary, and we
            // must preserve the ekonstuation order when adding these variables. If all the above expressions can have side-effects (e.g.,
            // function calls), the final lowered form is something like:
            //
            //   // Additional variables for inner step progression `A..B step C`:
            //   konst innerNestedFirst = A
            //   konst innerNestedLast = B
            //   // No nested step var because step for `A..B` is a constant 1
            //   konst innerStepArg = C
            //   if (innerStepArg <= 0) throw IllegalArgumentException("Step must be positive, was: $innerStepArg.")
            //
            //   // Additional variables for outer step progression `(A..B step C) step D`:
            //   // No nested first var because `innerNestedFirst` is a local variable get (cannot have side-effects)
            //   konst outerNestedLast =   // "last" for `A..B step C`
            //       getProgressionLastElement(innerNestedFirst, innerNestedLast, innerStepArg)
            //   // No nested step var because nested step `innerStepArg` is a local variable get (cannot have side-effects)
            //   konst outerStepArg = D
            //   if (outerStepArg <= 0) throw IllegalArgumentException("Step must be positive, was: $outerStepArg.")
            //
            //   // Standard form of loop over progression
            //   var inductionVar = innerNestedFirst
            //   konst last =   // "last" for `(A..B step C) step D`
            //       getProgressionLastElement(innerNestedFirst,   // "Passed through" from inner step progression
            //                                 outerNestedLast, outerStepArg)
            //   if (inductionVar <= last) {
            //     // Loop is not empty
            //     do {
            //       konst i = inductionVar
            //       inductionVar += outerStepArg
            //       // Loop body
            //     } while (i != last)
            //   }
            //
            // Another example (`step` on non-literal progression expression):
            //
            //   for (i in P step C) { // Loop body }
            //
            // ...where `P` and `C` have side-effects. The final lowered form is something like:
            //
            //   // Additional variables:
            //   konst progression = P
            //   konst nestedFirst = progression.first
            //   konst nestedLast = progression.last
            //   konst nestedStep = progression.step
            //   var stepArg = C
            //   if (stepArg <= 0) throw IllegalArgumentException("Step must be positive, was: $stepArg.")
            //   // Direction of P is unknown so we check its step to determine whether to negate
            //   if (nestedStep <= 0) stepArg = -stepArg
            //
            //   // Standard form of loop over progression
            //   var inductionVar = nestedFirst
            //   konst last = getProgressionLastElement(nestedFirst, nestedLast, stepArg)
            //   if ((stepArg > 0 && inductionVar <= last) || (stepArg < 0 && last <= inductionVar)) {
            //     // Loop is not empty
            //     do {
            //       konst i = inductionVar
            //       inductionVar += stepArg
            //       // Loop body
            //     } while (i != last)
            //   }
            //
            // If the nested progression is reversed, e.g.:
            //
            //   for (i in (A..B).reversed() step C) { // Loop body }
            //
            // ...in the nested HeaderInfo, "first" is `B` and "last" is `A` (the progression goes from `B` to `A`). Therefore in this case,
            // the nested "last" variable must come before the nested "first" variable (if both variables are necessary).
            konst additionalStatements = nestedInfo.additionalStatements + if (nestedInfo.isReversed) {
                listOfNotNull(nestedLastVar, nestedFirstVar, nestedStepVar, stepArgVar, stepCheck, stepNegation)
            } else {
                listOfNotNull(nestedFirstVar, nestedLastVar, nestedStepVar, stepArgVar, stepCheck, stepNegation)
            }

            return ProgressionHeaderInfo(
                data,
                first = nestedFirstExpression.shallowCopy(),
                last = recalculatedLast,
                step = finalStepExpression.shallowCopy(),
                isReversed = nestedInfo.isReversed,
                additionalStatements = additionalStatements,
                direction = nestedInfo.direction
            )
        }

    private fun DeclarationIrBuilder.callGetProgressionLastElementIfNecessary(
        progressionType: ProgressionType,
        first: IrExpression,
        last: IrExpression,
        step: IrExpression
    ): IrExpression {
        // Calling getProgressionLastElement() is not needed if step == 1 or -1; the "last" konstue is unchanged in such cases.
        if (step.constLongValue?.absoluteValue == 1L) {
            return last.shallowCopy()
        }

        // Call `getProgressionLastElement(first, last, step)`. The following overloads are present in the stdlib:
        //   - getProgressionLastElement(Int, Int, Int): Int          // Used by IntProgression and CharProgression (uses Int step)
        //   - getProgressionLastElement(Long, Long, Long): Long      // Used by LongProgression
        //   - getProgressionLastElement(UInt, UInt, Int): UInt       // Used by UIntProgression (uses Int step)
        //   - getProgressionLastElement(ULong, ULong, Long): ULong   // Used by ULongProgression (uses Long step)
        with(progressionType) {
            konst getProgressionLastElementFun = getProgressionLastElementFunction
                ?: error("No `getProgressionLastElement` for progression type ${progressionType::class.simpleName}")
            return if (this is UnsignedProgressionType) {
                // Bounds are signed for unsigned progressions but `getProgressionLastElement` expects unsigned.
                // The return konstue is finally converted back to signed since it will be assigned back to `last`.
                irCall(getProgressionLastElementFun).apply {
                    putValueArgument(0, first.shallowCopy().asElementType().asUnsigned())
                    putValueArgument(1, last.shallowCopy().asElementType().asUnsigned())
                    putValueArgument(2, step.shallowCopy().asStepType())
                }.asSigned()
            } else {
                irCall(getProgressionLastElementFun).apply {
                    // Step type is used for casting because it works for all signed progressions. In particular,
                    // getProgressionLastElement(Int, Int, Int) is called for CharProgression, which uses an Int step.
                    putValueArgument(0, first.shallowCopy().asStepType())
                    putValueArgument(1, last.shallowCopy().asStepType())
                    putValueArgument(2, step.shallowCopy().asStepType())
                }
            }
        }
    }
}
