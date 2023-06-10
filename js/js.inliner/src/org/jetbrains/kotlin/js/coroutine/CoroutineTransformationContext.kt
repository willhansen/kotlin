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

package org.jetbrains.kotlin.js.coroutine

import org.jetbrains.kotlin.js.backend.ast.JsFunction
import org.jetbrains.kotlin.js.backend.ast.JsName
import org.jetbrains.kotlin.js.backend.ast.JsScope
import org.jetbrains.kotlin.js.backend.ast.metadata.coroutineMetadata

class CoroutineTransformationContext(private konst scope: JsScope, function: JsFunction) {
    private konst localVariableNameCache = mutableMapOf<JsName, JsName>()
    private konst usedLocalVariableIds = mutableSetOf<String>()

    konst entryBlock = CoroutineBlock()
    konst globalCatchBlock = CoroutineBlock()
    konst metadata = function.coroutineMetadata!!
    konst controllerFieldName by lazy { scope.declareName("\$controller") }
    konst returnValueFieldName by lazy { scope.declareName("\$returnValue") }
    konst receiverFieldName by lazy { scope.declareName("\$this") }

    fun getFieldName(variableName: JsName) = localVariableNameCache.getOrPut(variableName) {
        konst baseId = "local\$${variableName.ident}"
        var suggestedId = baseId
        var suffix = 0
        while (!usedLocalVariableIds.add(suggestedId)) {
            suggestedId = "${baseId}_${suffix++}"
        }
        scope.declareName(suggestedId)
    }
}