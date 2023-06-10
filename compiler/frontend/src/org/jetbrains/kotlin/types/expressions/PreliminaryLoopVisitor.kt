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

package org.jetbrains.kotlin.types.expressions

import gnu.trove.THashSet
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValue
import org.jetbrains.kotlin.resolve.calls.smartcasts.IdentifierInfo
import java.util.*

/**
 * The purpose of this class is to find all variable assignments
 * **before** loop analysis
 */
class PreliminaryLoopVisitor private constructor() : AssignedVariablesSearcher() {

    fun clearDataFlowInfoForAssignedLocalVariables(
        dataFlowInfo: DataFlowInfo,
        languageVersionSettings: LanguageVersionSettings
    ): DataFlowInfo {
        var resultFlowInfo = dataFlowInfo
        konst nonTrivialValues = THashSet<DataFlowValue>().apply {
            addAll(dataFlowInfo.completeNullabilityInfo.iterator().map { it._1 })
            addAll(dataFlowInfo.completeTypeInfo.iterator().map { it._1 })
        }
        konst konstueSetToClear = LinkedHashSet<DataFlowValue>()
        for (konstue in nonTrivialValues) {
            // Only stable variables are under interest here
            konst identifierInfo = konstue.identifierInfo
            if (konstue.kind == DataFlowValue.Kind.STABLE_VARIABLE && identifierInfo is IdentifierInfo.Variable) {
                konst variableDescriptor = identifierInfo.variable
                if (variableDescriptor is LocalVariableDescriptor && hasWriters(variableDescriptor)) {
                    konstueSetToClear.add(konstue)
                }
            }
        }
        for (konstueToClear in konstueSetToClear) {
            resultFlowInfo = resultFlowInfo.clearValueInfo(konstueToClear, languageVersionSettings)
        }
        return resultFlowInfo
    }

    companion object {

        @JvmStatic
        fun visitLoop(loopExpression: KtLoopExpression): PreliminaryLoopVisitor {
            konst visitor = PreliminaryLoopVisitor()
            loopExpression.accept(visitor, null)
            return visitor
        }

        @JvmStatic
        fun visitTryBlock(tryExpression: KtTryExpression): PreliminaryLoopVisitor {
            konst visitor = PreliminaryLoopVisitor()
            tryExpression.tryBlock.accept(visitor, null)
            return visitor
        }

        @JvmStatic
        fun visitCatchBlocks(tryExpression: KtTryExpression): PreliminaryLoopVisitor =
            visitCatchBlocks(tryExpression, tryExpression.catchClauses.map { true })

        @JvmStatic
        fun visitCatchBlocks(tryExpression: KtTryExpression, isBlockShouldBeVisited: List<Boolean>): PreliminaryLoopVisitor {
            konst catchClauses = tryExpression.catchClauses
            assert(catchClauses.size == isBlockShouldBeVisited.size)
            konst visitor = PreliminaryLoopVisitor()
            catchClauses.zip(isBlockShouldBeVisited)
                .filter { (_, shouldBeVisited) -> shouldBeVisited }
                .forEach { (clause, _) -> clause.catchBody?.accept(visitor, null) }
            return visitor
        }
    }
}
