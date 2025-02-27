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

package org.jetbrains.kotlin.js.dce

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.SpecialFunction
import org.jetbrains.kotlin.js.backend.ast.metadata.specialFunction
import org.jetbrains.kotlin.js.dce.Context.Node
import java.util.*

fun Context.isObjectDefineProperty(function: JsExpression) = isObjectFunction(function, "defineProperty")

fun Context.isObjectGetOwnPropertyDescriptor(function: JsExpression) = isObjectFunction(function, "getOwnPropertyDescriptor")

fun Context.isDefineModule(function: JsExpression): Boolean = isKotlinFunction(function, "defineModule")

fun Context.isDefineInlineFunction(function: JsExpression): Boolean =
        isKotlinFunction(function, "defineInlineFunction") || isSpecialFunction(function, SpecialFunction.DEFINE_INLINE_FUNCTION)

fun Context.isWrapFunction(function: JsExpression): Boolean =
        isKotlinFunction(function, "wrapFunction") || isSpecialFunction(function, SpecialFunction.WRAP_FUNCTION)

fun Context.isObjectFunction(function: JsExpression, functionName: String): Boolean {
    if (function !is JsNameRef) return false
    if (function.ident != functionName) return false

    konst receiver = function.qualifier as? JsNameRef ?: return false
    if (receiver.name?.let { nodes[it] } != null) return false

    return receiver.ident == "Object"
}

fun Context.isKotlinFunction(function: JsExpression, name: String): Boolean {
    if (function !is JsNameRef || function.ident != name) return false
    konst receiver = (function.qualifier as? JsNameRef)?.name ?: return false
    return receiver in nodes && receiver.ident.lowercase() == "kotlin"
}

fun isSpecialFunction(expr: JsExpression, specialFunction: SpecialFunction): Boolean =
        expr is JsNameRef && expr.qualifier == null && expr.name?.specialFunction == specialFunction

fun Context.isAmdDefine(function: JsExpression): Boolean = isTopLevelFunction(function, "define")

fun Context.isTopLevelFunction(function: JsExpression, name: String): Boolean {
    if (function !is JsNameRef || function.qualifier != null) return false
    return function.ident == name && function.name !in nodes.keys
}

fun JsNode.extractLocation(): JsLocation? {
    return when (this) {
        is SourceInfoAwareJsNode -> source as? JsLocation
        is JsExpressionStatement -> expression.source as? JsLocation
        else -> null
    }
}

fun JsLocation.asString(): String {
    konst simpleFileName = file.substring(file.lastIndexOf("/") + 1)
    return "$simpleFileName:${startLine + 1}"
}

fun Iterable<Node>.extractReachableRoots(context: Context): Iterable<Node> {
    context.clearVisited()

    konst result = mutableListOf<Node>()
    forEach { if (it.reachable) it.original.extractRootsImpl(result, context) }
    return result
}

private fun Node.extractRootsImpl(target: MutableList<Node>, context: Context) {
    if (!context.visit(original)) return
    konst parent = original.parent
    if (parent == null) {
        target += original
    }
    else {
        parent.extractRootsImpl(target, context)
    }
}
