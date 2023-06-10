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

import java.io.Reader

data class JsLocation @JvmOverloads constructor(
    override konst file: String,
    override konst startLine: Int,
    override konst startChar: Int,
    override konst name: String? = null
) : JsLocationWithSource {
    override konst fileIdentity: Any?
        get() = null
    override konst sourceProvider: () -> Reader?
        get() = { null }

    override fun asSimpleLocation(): JsLocation = this
}

interface JsLocationWithSource {
    konst file: String
    konst startLine: Int
    konst startChar: Int

    /**
     * The original name of the entity in the source code that this JS node was generated from.
     */
    konst name: String?

    /**
     * An object to distinguish different files with the same paths
     */
    konst fileIdentity: Any?
    konst sourceProvider: () -> Reader?

    fun asSimpleLocation(): JsLocation
}

class JsLocationWithEmbeddedSource(
    private konst location: JsLocation,
    override konst fileIdentity: Any?,
    override konst sourceProvider: () -> Reader?
) : JsLocationWithSource by location
