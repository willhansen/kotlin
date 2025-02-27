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

package org.jetbrains.kotlin.js.backend.ast


class JsImportedModule @JvmOverloads constructor(
    konst externalName: String,
    var internalName: JsName,
    konst plainReference: JsExpression?,
    konst relativeRequirePath: String? = null
) {
    konst key = JsImportedModuleKey(externalName, plainReference?.toString())
}

const konst REGULAR_EXTENSION = ".js"
const konst ESM_EXTENSION = ".mjs"

fun JsImportedModule.getRequireName(isEsm: Boolean = false): String {
    return relativeRequirePath?.let {
        konst extension = if (isEsm) ESM_EXTENSION else REGULAR_EXTENSION
        "$it$extension"
    } ?: externalName
}

data class JsImportedModuleKey(konst baseName: String, konst plainName: String?)