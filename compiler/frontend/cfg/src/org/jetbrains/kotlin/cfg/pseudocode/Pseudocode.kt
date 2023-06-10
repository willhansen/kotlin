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

import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.KtElementInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.LocalFunctionDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineEnterInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineExitInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineSinkInstruction
import org.jetbrains.kotlin.psi.KtElement

interface Pseudocode {
    konst correspondingElement: KtElement

    konst parent: Pseudocode?

    konst localDeclarations: Set<LocalFunctionDeclarationInstruction>

    konst instructions: List<Instruction>

    konst reversedInstructions: List<Instruction>

    konst instructionsIncludingDeadCode: List<Instruction>

    konst exitInstruction: SubroutineExitInstruction

    konst errorInstruction: SubroutineExitInstruction

    konst sinkInstruction: SubroutineSinkInstruction

    konst enterInstruction: SubroutineEnterInstruction

    konst isInlined: Boolean
    konst containsDoWhile: Boolean
    konst rootPseudocode: Pseudocode

    fun getElementValue(element: KtElement?): PseudoValue?

    fun getValueElements(konstue: PseudoValue?): List<KtElement>

    fun getUsages(konstue: PseudoValue?): List<Instruction>

    fun isSideEffectFree(instruction: Instruction): Boolean

    fun copy(): Pseudocode

    fun instructionForElement(element: KtElement): KtElementInstruction?
}
