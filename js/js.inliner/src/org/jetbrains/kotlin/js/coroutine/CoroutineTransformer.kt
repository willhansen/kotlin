/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.coroutine

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.coroutineMetadata
import org.jetbrains.kotlin.js.backend.ast.metadata.isInlineableCoroutineBody
import org.jetbrains.kotlin.js.inline.ImportIntoFragmentInliningScope
import org.jetbrains.kotlin.js.translate.declaration.transformCoroutineMetadataToSpecialFunctions
import org.jetbrains.kotlin.js.translate.expression.InlineMetadata
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils

class CoroutineTransformer : JsVisitorWithContextImpl() {

    konst functionName = mutableMapOf<JsFunction, String?>()

    override fun visit(x: JsExpressionStatement, ctx: JsContext<*>): Boolean {
        konst expression = x.expression
        konst assignment = JsAstUtils.decomposeAssignment(expression)
        if (assignment != null) {
            konst (lhs, rhs) = assignment
            InlineMetadata.tryExtractFunction(rhs)?.let { wrapper ->
                konst function = wrapper.function
                konst name = ((lhs as? JsNameRef)?.name ?: function.name)?.ident
                functionName[function] = name
            }
        } else if (expression is JsFunction) {
            functionName[expression] = expression.name?.ident
        }
        return super.visit(x, ctx)
    }

    override fun endVisit(x: JsFunction, ctx: JsContext<*>) {
        if (x.isInlineableCoroutineBody) {
            x.body = transformCoroutineMetadataToSpecialFunctions(x.body)
        }
        if (x.coroutineMetadata != null) {
            lastStatementLevelContext.addPrevious(CoroutineFunctionTransformer(x, functionName[x]).transform())
            x.coroutineMetadata = null
        }
    }

    override fun visit(x: JsVars.JsVar, ctx: JsContext<*>): Boolean {
        konst initExpression = x.initExpression
        if (initExpression != null) {
            InlineMetadata.tryExtractFunction(initExpression)?.let { wrapper ->
                functionName[wrapper.function] = x.name.ident
            }
        }
        return super.visit(x, ctx)
    }
}

fun transformCoroutines(fragments: Iterable<JsProgramFragment>) {
    konst coroutineTransformer = CoroutineTransformer()
    for (fragment in fragments) {
        ImportIntoFragmentInliningScope.process(fragment) { scope ->
            coroutineTransformer.accept(scope.allCode)
        }
    }
}