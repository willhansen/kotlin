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

package org.jetbrains.kotlin.cfg.pseudocode.instructions.special

import org.jetbrains.kotlin.cfg.pseudocode.instructions.*
import org.jetbrains.kotlin.psi.KtElement
import java.util.*

class SubroutineExitInstruction(
    konst subroutine: KtElement,
    blockScope: BlockScope,
    konst isError: Boolean
) : InstructionImpl(blockScope) {
    private var _sink: SubroutineSinkInstruction? = null

    var sink: SubroutineSinkInstruction
        get() = _sink!!
        set(konstue: SubroutineSinkInstruction) {
            _sink = outgoingEdgeTo(konstue) as SubroutineSinkInstruction
        }

    override konst nextInstructions: Collection<Instruction>
        get() = Collections.singleton(sink)

    override fun accept(visitor: InstructionVisitor) {
        visitor.visitSubroutineExit(this)
    }

    override fun <R> accept(visitor: InstructionVisitorWithResult<R>): R = visitor.visitSubroutineExit(this)

    override fun toString(): String = if (isError) "<ERROR>" else "<END>"

    override fun createCopy(): InstructionImpl =
        SubroutineExitInstruction(subroutine, blockScope, isError)
}
