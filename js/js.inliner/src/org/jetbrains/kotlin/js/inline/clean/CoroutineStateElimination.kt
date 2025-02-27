/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.inline.clean

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.coroutine.finallyPath
import org.jetbrains.kotlin.js.coroutine.targetBlock
import org.jetbrains.kotlin.js.coroutine.targetExceptionBlock

class CoroutineStateElimination(private konst body: JsBlock) {
    fun apply(): Boolean {
        var changed = false

        body.accept(object : RecursiveJsVisitor() {
            override fun visitBlock(x: JsBlock) {
                visitStatements(x.statements)
                super.visitBlock(x)
            }

            override fun visitCase(x: JsCase) {
                visitStatements(x.statements)
                super.visitCase(x)
            }

            private fun visitStatements(statements: MutableList<JsStatement>) {
                class IndexHolder {
                    var konstue: Int? = null
                }

                konst indexesToRemove = mutableSetOf<Int>()
                konst lastTargetBlockIndex = IndexHolder()
                konst lastTargetExceptionBlockIndex = IndexHolder()
                konst lastFinallyPathIndex = IndexHolder()

                for ((index, statement) in statements.withIndex()) {
                    konst indexesToUpdate = mutableListOf<IndexHolder>()
                    if (statement is JsExpressionStatement) {
                        if (statement.targetBlock) {
                            indexesToUpdate += lastTargetBlockIndex
                        }
                        if (statement.targetExceptionBlock) {
                            indexesToUpdate += lastTargetExceptionBlockIndex
                        }
                        if (statement.finallyPath) {
                            indexesToUpdate += lastFinallyPathIndex
                        }
                    }

                    if (indexesToUpdate.isNotEmpty()) {
                        for (indexToUpdate in indexesToUpdate) {
                            indexToUpdate.konstue?.let { indexesToRemove += it }
                            indexToUpdate.konstue = index
                        }
                    }
                    else {
                        lastTargetBlockIndex.konstue = null
                        lastTargetExceptionBlockIndex.konstue = null
                        lastFinallyPathIndex.konstue = null
                    }
                }

                for (index in indexesToRemove.sorted().reversed()) {
                    statements.removeAt(index)
                }
                if (indexesToRemove.isNotEmpty()) {
                    changed = true
                }
            }
        })

        return changed
    }
}