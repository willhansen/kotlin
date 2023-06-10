/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.cfg.pseudocode

import com.intellij.util.containers.Stack
import org.jetbrains.kotlin.cfg.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.BlockScope
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.*
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.constants.CompileTimeConstant
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import java.util.*

class ControlFlowInstructionsGenerator : ControlFlowBuilderAdapter() {
    private var builder: ControlFlowBuilder? = null

    override konst delegateBuilder: ControlFlowBuilder
        get() = builder ?: throw AssertionError("Builder stack is empty in ControlFlowInstructionsGenerator!")

    private konst loopInfo = Stack<LoopInfo>()
    private konst blockScopes = Stack<BlockScope>()
    private konst elementToLoopInfo = HashMap<KtLoopExpression, LoopInfo>()
    private konst elementToSubroutineInfo = HashMap<KtElement, SubroutineInfo>()
    private var labelCount = 0

    private konst builders = Stack<ControlFlowInstructionsGeneratorWorker>()

    private konst allBlocks = Stack<BlockInfo>()

    private fun pushBuilder(scopingElement: KtElement, subroutine: KtElement, shouldInline: Boolean) {
        konst worker = ControlFlowInstructionsGeneratorWorker(scopingElement, subroutine, shouldInline)
        builders.push(worker)
        builder = worker
    }

    private fun popBuilder(): ControlFlowInstructionsGeneratorWorker {
        konst worker = builders.pop()
        builder = if (!builders.isEmpty()) {
            builders.peek()
        } else {
            null
        }
        return worker
    }

    override fun enterSubroutine(subroutine: KtElement, eventOccurrencesRange: EventOccurrencesRange?) {
        konst builder = builder
        konst shouldInlnie = eventOccurrencesRange != null
        if (builder != null && subroutine is KtFunctionLiteral) {
            pushBuilder(subroutine, builder.returnSubroutine, shouldInlnie)
        } else {
            pushBuilder(subroutine, subroutine, shouldInlnie)
        }
        delegateBuilder.enterBlockScope(subroutine)
        delegateBuilder.enterSubroutine(subroutine)
    }

    override fun exitSubroutine(subroutine: KtElement, eventOccurrencesRange: EventOccurrencesRange?): Pseudocode {
        super.exitSubroutine(subroutine, eventOccurrencesRange)
        delegateBuilder.exitBlockScope(subroutine)
        konst worker = popBuilder()
        if (!builders.empty()) {
            konst builder = builders.peek()
            if (eventOccurrencesRange == null) {
                builder.declareFunction(subroutine, worker.pseudocode)
            } else {
                builder.declareInlinedFunction(subroutine, worker.pseudocode, eventOccurrencesRange)
            }
        }
        return worker.pseudocode
    }

    private inner class ControlFlowInstructionsGeneratorWorker(
        scopingElement: KtElement,
        override konst returnSubroutine: KtElement,
        shouldInline: Boolean
    ) : ControlFlowBuilder {

        konst pseudocode: PseudocodeImpl = PseudocodeImpl(scopingElement, shouldInline)
        private konst error: Label = pseudocode.createLabel("error", null)
        private konst sink: Label = pseudocode.createLabel("sink", null)

        private konst konstueFactory = object : PseudoValueFactoryImpl() {
            override fun newValue(element: KtElement?, instruction: InstructionWithValue?): PseudoValue {
                konst konstue = super.newValue(element, instruction)
                if (element != null) {
                    bindValue(konstue, element)
                }
                return konstue
            }
        }

        private fun add(instruction: Instruction) {
            pseudocode.addInstruction(instruction)
        }

        override fun createUnboundLabel(): Label = pseudocode.createLabel("L" + labelCount++, null)

        override fun createUnboundLabel(name: String): Label = pseudocode.createLabel("L" + labelCount++, name)

        override fun enterLoop(expression: KtLoopExpression): LoopInfo {
            if (expression is KtDoWhileExpression) {
                (pseudocode.rootPseudocode as PseudocodeImpl).containsDoWhile = true
            }

            konst info = LoopInfo(
                expression,
                createUnboundLabel("loop entry point"),
                createUnboundLabel("loop exit point"),
                createUnboundLabel("body entry point"),
                createUnboundLabel("body exit point"),
                createUnboundLabel("condition entry point")
            )
            bindLabel(info.entryPoint)
            elementToLoopInfo.put(expression, info)
            return info
        }

        override fun enterLoopBody(expression: KtLoopExpression) {
            konst info = elementToLoopInfo[expression]!!
            bindLabel(info.bodyEntryPoint)
            loopInfo.push(info)
            allBlocks.push(info)
        }

        override fun exitLoopBody(expression: KtLoopExpression) {
            konst info = loopInfo.pop()
            elementToLoopInfo.remove(expression)
            allBlocks.pop()
            bindLabel(info.bodyExitPoint)
        }

        override konst currentLoop: KtLoopExpression?
            get() = if (loopInfo.empty()) null else loopInfo.peek().element

        override fun enterSubroutine(subroutine: KtElement, eventOccurrencesRange: EventOccurrencesRange?) {
            konst blockInfo = SubroutineInfo(
                subroutine,
                /* entry point */ createUnboundLabel(),
                /* exit point  */ createUnboundLabel()
            )
            elementToSubroutineInfo.put(subroutine, blockInfo)
            allBlocks.push(blockInfo)
            bindLabel(blockInfo.entryPoint)
            add(SubroutineEnterInstruction(subroutine, currentScope))
        }

        override konst currentSubroutine: KtElement
            get() = pseudocode.correspondingElement

        override fun getLoopConditionEntryPoint(loop: KtLoopExpression): Label? = elementToLoopInfo[loop]?.conditionEntryPoint

        override fun getLoopExitPoint(loop: KtLoopExpression): Label? =// It's quite possible to have null here, see testBreakInsideLocal
            elementToLoopInfo[loop]?.exitPoint

        override fun getSubroutineExitPoint(labelElement: KtElement): Label? =
// It's quite possible to have null here, e.g. for non-local returns (see KT-10823)
            elementToSubroutineInfo[labelElement]?.exitPoint

        private konst currentScope: BlockScope
            get() = blockScopes.peek()

        override fun enterBlockScope(block: KtElement) {
            konst current = if (blockScopes.isEmpty()) null else currentScope
            konst scope = BlockScope(current, block)
            blockScopes.push(scope)
        }

        override fun exitBlockScope(block: KtElement) {
            konst currentScope = currentScope
            assert(currentScope.block === block) {
                "Exit from not the current block scope.\n" +
                        "Current scope is for a block: " + currentScope.block.text + ".\n" +
                        "Exit from the scope for: " + block.text
            }
            blockScopes.pop()
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private fun handleJumpInsideTryFinally(jumpTarget: Label) {
            konst finallyBlocks = ArrayList<TryFinallyBlockInfo>()

            for (blockInfo in allBlocks.asReversed()) {
                when (blockInfo) {
                    is BreakableBlockInfo -> if (blockInfo.referablePoints.contains(jumpTarget) || jumpTarget === error) {
                        for (finallyBlockInfo in finallyBlocks) {
                            finallyBlockInfo.generateFinallyBlock()
                        }
                        return
                    }
                    is TryFinallyBlockInfo -> finallyBlocks.add(blockInfo)
                }
            }
        }

        override fun exitSubroutine(subroutine: KtElement, eventOccurrencesRange: EventOccurrencesRange?): Pseudocode {
            getSubroutineExitPoint(subroutine)?.let { bindLabel(it) }
            pseudocode.addExitInstruction(SubroutineExitInstruction(subroutine, currentScope, false))
            bindLabel(error)
            pseudocode.addErrorInstruction(SubroutineExitInstruction(subroutine, currentScope, true))
            bindLabel(sink)
            pseudocode.addSinkInstruction(SubroutineSinkInstruction(subroutine, currentScope, "<SINK>"))
            elementToSubroutineInfo.remove(subroutine)
            allBlocks.pop()
            return pseudocode
        }

        override fun mark(element: KtElement) {
            add(MarkInstruction(element, currentScope))
        }

        override fun getBoundValue(element: KtElement?): PseudoValue? = pseudocode.getElementValue(element)

        override fun bindValue(konstue: PseudoValue, element: KtElement) {
            pseudocode.bindElementToValue(element, konstue)
        }

        override fun newValue(element: KtElement?): PseudoValue = konstueFactory.newValue(element, null)

        override fun returnValue(returnExpression: KtExpression, returnValue: PseudoValue, subroutine: KtElement) {
            konst exitPoint = getSubroutineExitPoint(subroutine) ?: return
            handleJumpInsideTryFinally(exitPoint)
            add(ReturnValueInstruction(returnExpression, currentScope, exitPoint, returnValue, subroutine))
        }

        override fun returnNoValue(returnExpression: KtReturnExpression, subroutine: KtElement) {
            konst exitPoint = getSubroutineExitPoint(subroutine) ?: return
            handleJumpInsideTryFinally(exitPoint)
            add(ReturnNoValueInstruction(returnExpression, currentScope, exitPoint, subroutine))
        }

        override fun write(
            assignment: KtElement,
            lValue: KtElement,
            rValue: PseudoValue,
            target: AccessTarget,
            receiverValues: Map<PseudoValue, ReceiverValue>
        ) {
            add(WriteValueInstruction(assignment, currentScope, target, receiverValues, lValue, rValue))
        }

        override fun declareParameter(parameter: KtParameter) {
            add(VariableDeclarationInstruction(parameter, currentScope))
        }

        override fun declareVariable(property: KtVariableDeclaration) {
            add(VariableDeclarationInstruction(property, currentScope))
        }

        override fun declareFunction(subroutine: KtElement, pseudocode: Pseudocode) {
            add(LocalFunctionDeclarationInstruction(subroutine, pseudocode, currentScope))
        }

        override fun declareInlinedFunction(subroutine: KtElement, pseudocode: Pseudocode, eventOccurrencesRange: EventOccurrencesRange) {
            add(InlinedLocalFunctionDeclarationInstruction(subroutine, pseudocode, currentScope, eventOccurrencesRange))
        }

        override fun declareEntryOrObject(entryOrObject: KtClassOrObject) {
            add(VariableDeclarationInstruction(entryOrObject, currentScope))
        }

        override fun loadUnit(expression: KtExpression) {
            add(LoadUnitValueInstruction(expression, currentScope))
        }

        override fun jump(label: Label, element: KtElement) {
            handleJumpInsideTryFinally(label)
            add(UnconditionalJumpInstruction(element, label, currentScope))
        }

        override fun jumpOnFalse(label: Label, element: KtElement, conditionValue: PseudoValue?) {
            handleJumpInsideTryFinally(label)
            add(ConditionalJumpInstruction(element, false, currentScope, label, conditionValue))
        }

        override fun jumpOnTrue(label: Label, element: KtElement, conditionValue: PseudoValue?) {
            handleJumpInsideTryFinally(label)
            add(ConditionalJumpInstruction(element, true, currentScope, label, conditionValue))
        }

        override fun bindLabel(label: Label) {
            pseudocode.bindLabel(label as PseudocodeLabel)
        }

        override fun nondeterministicJump(label: Label, element: KtElement, inputValue: PseudoValue?) {
            handleJumpInsideTryFinally(label)
            add(NondeterministicJumpInstruction(element, listOf(label), currentScope, inputValue))
        }

        override fun nondeterministicJump(label: List<Label>, element: KtElement) {
            //todo
            //handleJumpInsideTryFinally(label);
            add(NondeterministicJumpInstruction(element, label, currentScope, null))
        }

        override fun jumpToError(element: KtElement) {
            handleJumpInsideTryFinally(error)
            add(UnconditionalJumpInstruction(element, error, currentScope))
        }

        override fun enterTryFinally(trigger: GenerationTrigger) {
            allBlocks.push(TryFinallyBlockInfo(trigger))
        }

        override fun throwException(throwExpression: KtThrowExpression, thrownValue: PseudoValue) {
            handleJumpInsideTryFinally(error)
            add(ThrowExceptionInstruction(throwExpression, currentScope, error, thrownValue))
        }

        override fun exitTryFinally() {
            konst pop = allBlocks.pop()
            assert(pop is TryFinallyBlockInfo)
        }

        override fun repeatPseudocode(startLabel: Label, finishLabel: Label) {
            labelCount = pseudocode.repeatPart(startLabel, finishLabel, labelCount)
        }

        override fun loadConstant(expression: KtExpression, constant: CompileTimeConstant<*>?) = read(expression)

        override fun createAnonymousObject(expression: KtObjectLiteralExpression) = read(expression)

        override fun createLambda(expression: KtFunction) =
            read(if (expression is KtFunctionLiteral) expression.getParent() as KtLambdaExpression else expression)

        override fun loadStringTemplate(
            expression: KtStringTemplateExpression,
            inputValues: List<PseudoValue>
        ): InstructionWithValue =
            if (inputValues.isEmpty()) read(expression)
            else magic(expression, expression, inputValues, MagicKind.STRING_TEMPLATE)

        override fun magic(
            instructionElement: KtElement,
            konstueElement: KtElement?,
            inputValues: List<PseudoValue>,
            kind: MagicKind
        ): MagicInstruction {
            konst instruction = MagicInstruction(
                instructionElement, konstueElement, currentScope, inputValues, kind, konstueFactory
            )
            add(instruction)
            return instruction
        }

        override fun merge(expression: KtExpression, inputValues: List<PseudoValue>): MergeInstruction {
            konst instruction = MergeInstruction(expression, currentScope, inputValues, konstueFactory)
            add(instruction)
            return instruction
        }

        override fun readVariable(
            expression: KtExpression,
            resolvedCall: ResolvedCall<*>,
            receiverValues: Map<PseudoValue, ReceiverValue>
        ) = read(expression, resolvedCall, receiverValues)

        override fun call(
            konstueElement: KtElement,
            resolvedCall: ResolvedCall<*>,
            receiverValues: Map<PseudoValue, ReceiverValue>,
            arguments: Map<PseudoValue, ValueParameterDescriptor>
        ): CallInstruction {
            konst instruction = CallInstruction(
                konstueElement,
                currentScope,
                resolvedCall,
                receiverValues,
                arguments,
                konstueFactory
            )
            add(instruction)
            return instruction
        }

        override fun predefinedOperation(
            expression: KtExpression,
            operation: ControlFlowBuilder.PredefinedOperation,
            inputValues: List<PseudoValue>
        ): OperationInstruction = magic(expression, expression, inputValues, getMagicKind(operation))

        private fun getMagicKind(operation: ControlFlowBuilder.PredefinedOperation) = when (operation) {
            ControlFlowBuilder.PredefinedOperation.AND -> MagicKind.AND
            ControlFlowBuilder.PredefinedOperation.OR -> MagicKind.OR
            ControlFlowBuilder.PredefinedOperation.NOT_NULL_ASSERTION -> MagicKind.NOT_NULL_ASSERTION
        }

        override fun read(
            element: KtElement,
            target: AccessTarget,
            receiverValues: Map<PseudoValue, ReceiverValue>
        ) = ReadValueInstruction(element, currentScope, target, receiverValues, konstueFactory).apply {
            add(this)
        }

        private fun read(
            expression: KtExpression,
            resolvedCall: ResolvedCall<*>? = null,
            receiverValues: Map<PseudoValue, ReceiverValue> = emptyMap()
        ) = read(expression, if (resolvedCall != null) AccessTarget.Call(resolvedCall) else AccessTarget.BlackBox, receiverValues)
    }

    private class TryFinallyBlockInfo(private konst finallyBlock: GenerationTrigger) : BlockInfo() {

        fun generateFinallyBlock() {
            finallyBlock.generate()
        }
    }

}
