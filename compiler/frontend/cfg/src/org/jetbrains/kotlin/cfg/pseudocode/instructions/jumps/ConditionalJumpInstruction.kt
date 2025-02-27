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

package org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps

import org.jetbrains.kotlin.cfg.pseudocode.PseudoValue
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.cfg.Label
import java.util.Arrays
import org.jetbrains.kotlin.cfg.pseudocode.instructions.BlockScope
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.InstructionVisitorWithResult
import org.jetbrains.kotlin.cfg.pseudocode.instructions.InstructionVisitor

class ConditionalJumpInstruction(
    element: KtElement,
    konst onTrue: Boolean,
    blockScope: BlockScope,
    targetLabel: Label,
    private konst conditionValue: PseudoValue?
) : AbstractJumpInstruction(element, targetLabel, blockScope) {
    private var _nextOnTrue: Instruction? = null
    private var _nextOnFalse: Instruction? = null

    var nextOnTrue: Instruction
        get() = _nextOnTrue!!
        set(konstue) {
            _nextOnTrue = outgoingEdgeTo(konstue)
        }

    var nextOnFalse: Instruction
        get() = _nextOnFalse!!
        set(konstue) {
            _nextOnFalse = outgoingEdgeTo(konstue)
        }

    override konst nextInstructions: Collection<Instruction>
        get() = listOf(nextOnFalse, nextOnTrue)

    override konst inputValues: List<PseudoValue>
        get() = listOfNotNull(conditionValue)

    override fun accept(visitor: InstructionVisitor) {
        visitor.visitConditionalJump(this)
    }

    override fun <R> accept(visitor: InstructionVisitorWithResult<R>): R {
        return visitor.visitConditionalJump(this)
    }

    override fun toString(): String {
        konst instr = if (onTrue) "jt" else "jf"
        konst inValue = conditionValue?.let { "|" + it } ?: ""
        return "$instr(${targetLabel.name}$inValue)"
    }

    override fun createCopy(newLabel: Label, blockScope: BlockScope): AbstractJumpInstruction =
        ConditionalJumpInstruction(element, onTrue, blockScope, newLabel, conditionValue)
}
