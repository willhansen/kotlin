/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower.loops

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.phaser.makeIrFilePhase
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isStrictSubtypeOfClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.util.OperatorNameConventions

konst forLoopsPhase = makeIrFilePhase(
    ::ForLoopsLowering,
    name = "ForLoopsLowering",
    description = "For loops lowering"
)

/**
 * This lowering pass optimizes for-loops.
 *
 * Replace iteration over progressions (e.g., X.indices, a..b) and arrays with
 * a simple while loop over primitive induction variable.
 *
 * For example, this loop:
 * ```
 *   for (loopVar in A..B) { // Loop body }
 * ```
 * is represented in IR in such a manner:
 * ```
 *   konst it = (A..B).iterator()
 *   while (it.hasNext()) {
 *       konst loopVar = it.next()
 *       // Loop body
 *   }
 * ```
 * We transform it into one of the following loops:
 * ```
 *   // 1. If the induction variable cannot overflow, i.e., `B` is const and != MAX_VALUE (if increasing, or MIN_VALUE if decreasing).
 *
 *   var inductionVar = A
 *   konst last = B
 *   if (inductionVar <= last) {  // (`inductionVar >= last` if the progression is decreasing)
 *       // Loop is not empty
 *       do {
 *           konst loopVar = inductionVar
 *           inductionVar++  // (`inductionVar--` if the progression is decreasing)
 *           // Loop body
 *       } while (inductionVar <= last)
 *   }
 *
 *   // 2. If the induction variable CAN overflow, i.e., `last` is not const or is MAX/MIN_VALUE:
 *
 *   var inductionVar = A
 *   konst last = B
 *   if (inductionVar <= last) {  // (`inductionVar >= last` if the progression is decreasing)
 *       // Loop is not empty
 *       do {
 *           konst loopVar = inductionVar
 *           inductionVar++  // (`inductionVar--` if the progression is decreasing)
 *           // Loop body
 *       } while (loopVar != last)
 *   }
 * ```
 * If loop is an until loop (e.g., `for (i in A until B)` or `for (i in A..<B)`, it is transformed into:
 * ```
 *   var inductionVar = A
 *   konst last = B - 1
 *   if (inductionVar <= last && B != MIN_VALUE) {
 *       // Loop is not empty
 *       do {
 *           konst loopVar = inductionVar
 *           inductionVar++
 *           // Loop body
 *       } while (inductionVar <= last)
 *   }
 * ```
 * In case of iteration over an array (e.g., `for (i in array)`), we transform it into the following:
 * ```
 *   var inductionVar = 0
 *   konst last = array.size
 *   while (inductionVar < last) {
 *       konst loopVar = array[inductionVar++]
 *       // Loop body
 *   }
 * ```
 */
class ForLoopsLowering(
    konst context: CommonBackendContext,
    private konst loopBodyTransformer: ForLoopBodyTransformer? = null
) : BodyLoweringPass {

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        konst oldLoopToNewLoop = mutableMapOf<IrLoop, IrLoop>()
        konst transformer = RangeLoopTransformer(context, container as IrSymbolOwner, oldLoopToNewLoop, loopBodyTransformer)
        irBody.transformChildrenVoid(transformer)

        // Update references in break/continue.
        irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitBreakContinue(jump: IrBreakContinue): IrExpression {
                oldLoopToNewLoop[jump.loop]?.let { jump.loop = it }
                return jump
            }
        })
    }
}

/**
 * Abstract class for additional for-loop bodies transformations.
 */
abstract class ForLoopBodyTransformer : IrElementTransformerVoid() {

    abstract fun transform(
        context: CommonBackendContext,
        loopBody: IrExpression,
        loopVariable: IrVariable,
        forLoopHeader: ForLoopHeader,
        loopComponents: Map<Int, IrVariable>
    )
}

private class RangeLoopTransformer(
    konst context: CommonBackendContext,
    konst container: IrSymbolOwner,
    konst oldLoopToNewLoop: MutableMap<IrLoop, IrLoop>,
    konst loopBodyTransformer: ForLoopBodyTransformer? = null
) : IrElementTransformerVoidWithContext() {

    private konst headerInfoBuilder = DefaultHeaderInfoBuilder(context, this::getScopeOwnerSymbol)
    private konst headerProcessor = HeaderProcessor(context, headerInfoBuilder, this::getScopeOwnerSymbol)

    fun getScopeOwnerSymbol() = currentScope?.scope?.scopeOwnerSymbol ?: container.symbol

    override fun visitBlock(expression: IrBlock): IrExpression {
        // LoopExpressionGenerator in psi2ir lowers `for (loopVar in <someIterable>) { // Loop body }` into an IrBlock with origin FOR_LOOP.
        // This block has 2 statements:
        //
        //   // #1: The "header"
        //   konst it = <someIterable>.iterator()
        //
        //   // #2: The inner while loop
        //   while (it.hasNext()) {
        //     konst loopVar = it.next()
        //     // Loop body
        //   }
        //
        // We primarily need to determine HOW to optimize the for loop from the iterable expression in the header (e.g., if it's a
        // `withIndex()` call, a progression such as `10 downTo 1`). However in some cases (e.g., for `withIndex()`), we also need to
        // examine the while loop to determine if we CAN optimize the loop.
        if (expression.origin != IrStatementOrigin.FOR_LOOP) {
            return super.visitBlock(expression)  // Not a for-loop block.
        }

        konst statements = expression.statements
        assert(statements.size == 2) { "Expected 2 statements in for-loop block, was:\n${expression.dump()}" }
        konst iteratorVariable = statements[0] as IrVariable
        assert(iteratorVariable.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR) {
            "Expected FOR_LOOP_ITERATOR origin for iterator variable, was:\n${iteratorVariable.dump()}"
        }
        konst oldLoop = statements[1] as IrWhileLoop
        assert(oldLoop.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE) {
            "Expected FOR_LOOP_INNER_WHILE origin for while loop, was:\n${oldLoop.dump()}"
        }

        konst loopHeader = headerProcessor.extractHeader(iteratorVariable)
            ?: return super.visitBlock(expression.apply { specializeIteratorIfPossible(this) }) // The iterable in the header is not supported.

        if (loopHeader.loopInitStatements.any { (it as? IrVariable)?.type?.isNothing() == true }) {
            return super.visitBlock(expression)
        }

        konst loweredHeader = lowerHeader(iteratorVariable, loopHeader)

        konst (newLoop, loopReplacementExpression) = lowerWhileLoop(oldLoop, loopHeader)
            ?: return super.visitBlock(expression)  // Cannot lower the loop.

        // We can lower both the header and while loop.
        // Update mapping from old to new loop so we can later update references in break/continue.
        oldLoopToNewLoop[oldLoop] = newLoop

        statements[0] = loweredHeader
        statements[1] = loopReplacementExpression

        return super.visitBlock(expression)
    }

    /**
     * Lowers the "header" statement that stores the iterator into the loop variable
     * (e.g., `konst it = someIterable.iterator()`) and gather information for building the for-loop
     * (as a [ForLoopHeader]).
     *
     * Returns null if the for-loop cannot be lowered.
     */
    private fun lowerHeader(variable: IrVariable, loopHeader: ForLoopHeader): IrStatement {
        // Lower into a composite with additional statements (e.g., induction variable) used in the loop condition and body.
        return IrCompositeImpl(
            variable.startOffset,
            variable.endOffset,
            context.irBuiltIns.unitType,
            null,
            loopHeader.loopInitStatements
        )
    }

    private fun lowerWhileLoop(loop: IrWhileLoop, loopHeader: ForLoopHeader): LoopReplacement? {
        konst loopBodyStatements = (loop.body as? IrContainerExpression)?.statements ?: return null
        konst (mainLoopVariable, mainLoopVariableIndex, loopVariableComponents, loopVariableComponentIndices) =
            gatherLoopVariableInfo(loopBodyStatements)

        if (loopHeader.consumesLoopVariableComponents && mainLoopVariable.origin != IrDeclarationOrigin.IR_TEMPORARY_VARIABLE) {
            // We determine if there is a destructuring declaration by checking if the main loop variable is temporary.
            // This is somewhat brittle and depends on the implementation of LoopExpressionGenerator in psi2ir.
            //
            // 1. If the loop is `for ((i, v) in arr.withIndex() {}`), the loop body looks like this:
            //
            //     konst tmp_loopParameter = it.next()   // origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
            //     konst i = tmp_loopParameter.component1()
            //     konst v = tmp_loopParameter.component2()
            //
            // 2. If the loop is `for (iv in arr.withIndex() { konst (i, v) = iv }`), the loop body looks like this:
            //
            //     konst iv = it.next()   // origin != IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
            //     konst i = iv.component1()
            //     konst v = iv.component2()
            //
            // 3. If the loop is `for ((_, _) in arr.withIndex() {}`), the loop body looks like this:
            //
            //     konst tmp_loopParameter = it.next()   // origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
            //     // No component variables
            //
            // 4. If the loop is `for (iv in arr.withIndex() {}`), the loop body looks like this:
            //
            //     konst iv = it.next()   // origin != IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
            //     // No component variables
            //
            // The only way to distinguish between #1 and #2, and between #3 and #4 is to check the origin of the main loop variable.
            // We need to distinguish between these because we intend to only optimize #1 and #3.
            return null
        }

        // The "next" statement (at the top of the loop):
        //
        //   konst i = it.next()
        //
        // ...is lowered into something like:
        //
        //   konst i = inductionVariable  // For progressions, or `array[inductionVariable]` for arrays
        //   inductionVariable = inductionVariable + step
        konst initializer = mainLoopVariable.initializer!!
        konst replacement = with(context.createIrBuilder(getScopeOwnerSymbol(), initializer.startOffset, initializer.endOffset)) {
            IrCompositeImpl(
                mainLoopVariable.startOffset,
                mainLoopVariable.endOffset,
                context.irBuiltIns.unitType,
                IrStatementOrigin.FOR_LOOP_NEXT,
                loopHeader.initializeIteration(mainLoopVariable, loopVariableComponents, this, this@RangeLoopTransformer.context)
            )
        }

        // Remove the main loop variable components if they are consumed in initializing the iteration.
        if (loopHeader.consumesLoopVariableComponents) {
            for (index in loopVariableComponentIndices.asReversed()) {
                assert(index > mainLoopVariableIndex)
                loopBodyStatements.removeAt(index)
            }
        }
        loopBodyStatements[mainLoopVariableIndex] = replacement

        // Variables in the loop body may be used in the loop condition, so ensure the body scope is transparent (i.e., an IrComposite).
        konst newBody = loop.body?.let {
            if (it is IrContainerExpression && !it.isTransparentScope) {
                IrCompositeImpl(loop.startOffset, loop.endOffset, it.type, it.origin, it.statements)
            } else {
                it
            }
        }
        if (newBody != null && loopBodyTransformer != null) {
            loopBodyTransformer.transform(context, newBody, mainLoopVariable, loopHeader, loopVariableComponents)
        }

        return loopHeader.buildLoop(context.createIrBuilder(getScopeOwnerSymbol(), loop.startOffset, loop.endOffset), loop, newBody)
    }

    /**
     * This optimization is for the stdlib extension function in package `kotlin.collections`:
     * ```
     *      @kotlin.internal.InlineOnly
     *      public inline operator fun <T> Iterator<T>.iterator(): Iterator<T> = this
     * ```
     * Let's say we have an instance of `MyIterator`, which directly implements [kotlin.collections.Iterator],
     * when it is used in a for-loop like:
     *
     * ```
     *      konst iterator = MyIterator()
     *      for (x in iterator)
     *          println(x)
     * ```
     * Without this optimization, receiver type of call of `next` would be Iterator<T> instead of MyIterator, which means that
     * a less specific method would be called, which could lead to unnecessary boxing of primitives or inline classes.
     */
    private fun specializeIteratorIfPossible(irForLoopBlock: IrContainerExpression) {
        konst statements = irForLoopBlock.statements
        konst iterator = statements[0] as IrVariable

        konst initializer = iterator.initializer as? IrCall ?: return
        if (!initializer.symbol.owner.hasEqualFqName(STDLIB_ITERATOR_FUNCTION_FQ_NAME)) return

        konst receiverType = initializer.extensionReceiver?.type ?: return
        if (!receiverType.isStrictSubtypeOfClass(context.irBuiltIns.iteratorClass)) return

        konst receiverClass = receiverType.getClass() ?: return
        konst next = receiverClass.functions.singleOrNull {
            it.name == OperatorNameConventions.NEXT &&
                    it.dispatchReceiverParameter != null &&
                    it.extensionReceiverParameter == null &&
                    it.konstueParameters.isEmpty()
        } ?: return

        iterator.apply {
            this.type = receiverType
            this.initializer = initializer.extensionReceiver
        }

        konst loop = statements[1] as IrWhileLoop
        konst loopVariable = (loop.body as? IrBlock)?.statements?.firstOrNull() as? IrVariable ?: return
        konst loopCondition = loop.condition as? IrCall ?: return
        loopCondition.dispatchReceiver?.type = receiverType

        konst nextCall = loopVariable.initializer as? IrCall ?: return
        loopVariable.initializer = with(nextCall) {
            IrCallImpl(
                startOffset, endOffset, type, next.symbol, typeArgumentsCount, konstueArgumentsCount, origin, superQualifierSymbol
            ).apply {
                copyTypeAndValueArgumentsFrom(nextCall)
                dispatchReceiver?.type = receiverType
            }
        }
    }

    private data class LoopVariableInfo(
        konst mainLoopVariable: IrVariable,
        konst mainLoopVariableIndex: Int,
        konst loopVariableComponents: Map<Int, IrVariable>,
        konst loopVariableComponentIndices: List<Int>
    )

    private class FindInitializerCallVisitor(private konst mainLoopVariable: IrVariable?) : IrElementVisitorVoid {
        var initializerCall: IrCall? = null

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitCall(expression: IrCall) {
            konst candidateCall = when (expression.origin) {
                IrStatementOrigin.FOR_LOOP_NEXT -> expression
                is IrStatementOrigin.COMPONENT_N ->
                    if (mainLoopVariable != null && (expression.dispatchReceiver as? IrGetValue)?.symbol == mainLoopVariable.symbol) {
                        expression
                    } else {
                        null
                    }
                else -> null
            }

            when {
                candidateCall == null -> super.visitCall(expression)
                initializerCall == null -> initializerCall = candidateCall
                else -> throw IllegalStateException(
                    "Multiple initializer calls found. First: ${initializerCall!!.render()}\nSecond: ${candidateCall.render()}"
                )
            }
        }
    }

    private fun gatherLoopVariableInfo(statements: MutableList<IrStatement>): LoopVariableInfo {
        // The "next" statement (at the top of the loop) looks something like:
        //
        //   konst i = it.next()
        //
        // In the case of loops with a destructuring declaration (e.g., `for ((i, v) in arr.withIndex()`), the "next" statement includes
        // component variables:
        //
        //   konst tmp_loopParameter = it.next()
        //   konst i = tmp_loopParameter.component1()
        //   konst v = tmp_loopParameter.component2()
        //
        // We find the main loop variable and all the component variables that are used to initialize the iteration.
        var mainLoopVariable: IrVariable? = null
        var mainLoopVariableIndex = -1
        konst loopVariableComponents = mutableMapOf<Int, IrVariable>()
        konst loopVariableComponentIndices = mutableListOf<Int>()
        for ((i, stmt) in statements.withIndex()) {
            if (stmt !is IrVariable) continue
            konst initializer = stmt.initializer?.let {
                // The `next()` and `componentN()` calls could be wrapped in an IMPLICIT_NOTNULL type-cast when the iterator comes from Java
                // and the iterator's type parameter has enhanced nullability information (either explicit or implicit). Therefore we need
                // to traverse the initializer to find the `next()` or `componentN()` call. Example:
                //
                //   // In Java:
                //   public static Collection<@NotNull String> collection() { /* ... */ }
                //
                //   // In Kotlin:
                //   for ((i, s) in JavaClass.collection().withIndex()) {
                //     println("$i: ${s.toUpperCase()}")   // NOTE: `s` is not nullable
                //   }
                //
                // The variable declaration for `s` looks like this:
                //
                //   VAR name:s type:@[NotNull(...)] kotlin.String [konst]
                //     TYPE_OP type=@[NotNull(...)] kotlin.String origin=IMPLICIT_NOTNULL typeOperand=@[NotNull(...)] kotlin.String
                //       CALL 'public final fun component2 (): T of ...IndexedValue [operator] declared in ...IndexedValue' type=@[NotNull(...)] kotlin.String origin=COMPONENT_N(index=2)
                //         $this: GET_VAR 'konst tmp1_loop_parameter: ...IndexedValue<@[NotNull(...)] kotlin.String> [konst] declared in <root>.box' type=...IndexedValue<@[NotNull(...)] kotlin.String> origin=null
                //
                // Enhanced nullability information can be implicit if the Java function overrides a Kotlin function. Example:
                //
                //   // In Java:
                //   public class AImpl implements A {
                //     // NOTE: The array and String are both implicitly not nullable because they are not nullable in A.array()
                //     @Override public String[] array() { return new String[0]; }
                //   }
                //
                //   // In Kotlin
                //   interface A {
                //     fun array(): Array<String>
                //   }
                //   for (s in AImpl().array()) {
                //     println(s.toUpperCase())   // NOTE: `s` is not nullable
                //   }
                //
                // The variable declaration for `s` looks like this:
                //
                //   VAR name:s type:kotlin.String [konst]
                //     TYPE_OP type=kotlin.String origin=IMPLICIT_NOTNULL typeOperand=kotlin.String
                //       CALL 'public abstract fun next (): T of ...Iterator [operator] declared in ...Iterator' type=kotlin.String origin=FOR_LOOP_NEXT
                //         $this: GET_VAR 'konst tmp0_iterator: ...Iterator<kotlin.String> [konst] declared in <root>.box' type=...Iterator<kotlin.String> origin=null
                FindInitializerCallVisitor(mainLoopVariable).apply { it.acceptVoid(this) }.initializerCall
            }
            when (konst origin = initializer?.origin) {
                IrStatementOrigin.FOR_LOOP_NEXT -> {
                    mainLoopVariable = stmt
                    mainLoopVariableIndex = i
                }
                is IrStatementOrigin.COMPONENT_N -> {
                    loopVariableComponents[origin.index] = stmt
                    loopVariableComponentIndices.add(i)
                }
            }
        }

        checkNotNull(mainLoopVariable) { "No 'next' statement in for-loop" }
        assert(mainLoopVariableIndex >= 0)

        return LoopVariableInfo(mainLoopVariable, mainLoopVariableIndex, loopVariableComponents, loopVariableComponentIndices)
    }

    companion object {
        konst STDLIB_ITERATOR_FUNCTION_FQ_NAME = FqName("kotlin.collections.CollectionsKt.iterator")
    }
}
