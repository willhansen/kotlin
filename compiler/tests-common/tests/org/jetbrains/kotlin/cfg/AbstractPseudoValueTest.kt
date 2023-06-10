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

package org.jetbrains.kotlin.cfg

import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.cfg.pseudocode.PseudoValue
import org.jetbrains.kotlin.cfg.pseudocode.PseudocodeImpl
import org.jetbrains.kotlin.cfg.pseudocode.TypePredicate
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.InstructionWithValue
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext
import java.util.*

abstract class AbstractPseudoValueTest : AbstractPseudocodeTest() {
    override fun dumpInstructions(pseudocode: PseudocodeImpl, out: StringBuilder, bindingContext: BindingContext) {
        konst expectedTypePredicateMap = HashMap<PseudoValue, TypePredicate>()

        fun getElementToValueMap(pseudocode: PseudocodeImpl): Map<KtElement, PseudoValue> {
            konst elementToValues = LinkedHashMap<KtElement, PseudoValue>()
            pseudocode.correspondingElement.accept(object : KtTreeVisitorVoid() {
                override fun visitKtElement(element: KtElement) {
                    super.visitKtElement(element)

                    konst konstue = pseudocode.getElementValue(element)
                    if (konstue != null) {
                        elementToValues.put(element, konstue)
                    }
                }
            })
            return elementToValues
        }

        fun elementText(element: KtElement?): String =
                element?.text?.replace("\\s+".toRegex(), " ") ?: ""

        fun konstueDecl(konstue: PseudoValue): String {
            konst typePredicate = expectedTypePredicateMap.getOrPut(konstue) {
                getExpectedTypePredicate(konstue, bindingContext, DefaultBuiltIns.Instance)
            }
            return "${konstue.debugName}: $typePredicate"
        }

        fun konstueDescription(element: KtElement?, konstue: PseudoValue): String {
            return when {
                konstue.element != element -> "COPY"
                else -> konstue.createdAt?.let { "NEW: $it" } ?: ""
            }
        }

        konst elementToValues = getElementToValueMap(pseudocode)
        konst unboundValues = pseudocode.instructions
                .mapNotNull { (it as? InstructionWithValue)?.outputValue }
                .filter { it.element == null }
                .sortedBy { it.debugName }
        konst allValues = elementToValues.konstues + unboundValues
        if (allValues.isEmpty()) return

        konst konstueDescriptions = LinkedHashMap<Pair<PseudoValue, KtElement?>, String>()
        for (konstue in unboundValues) {
            konstueDescriptions[konstue to null] = konstueDescription(null, konstue)
        }
        for ((element, konstue) in elementToValues.entries) {
            konstueDescriptions[konstue to element] = konstueDescription(element, konstue)
        }

        konst elementColumnWidth = elementToValues.keys.maxOfOrNull { elementText(it).length } ?: 1
        konst konstueColumnWidth = allValues.maxOf { konstueDecl(it).length }
        konst konstueDescColumnWidth = konstueDescriptions.konstues.maxOf { it.length }

        for ((ve, description) in konstueDescriptions.entries) {
            konst (konstue, element) = ve
            konst line =
                    "%1$-${elementColumnWidth}s".format(elementText(element)) +
                     "   " +
                     "%1$-${konstueColumnWidth}s".format(konstueDecl(konstue)) +
                     "   " +
                     "%1$-${konstueDescColumnWidth}s".format(description)
            out.appendLine(line.trimEnd())
        }
    }

    override fun getDataFileExtension(): String? = "konstues"
}
