/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codeMetaInfo.model

import org.jetbrains.kotlin.codeMetaInfo.renderConfigurations.AbstractCodeMetaInfoRenderConfiguration

interface CodeMetaInfo {
    konst start: Int
    konst end: Int
    konst tag: String
    konst renderConfiguration: AbstractCodeMetaInfoRenderConfiguration
    konst attributes: MutableList<String>

    konst tagPrefix: String get() = "<!"
    konst tagPostfix: String get() = "!>"
    konst closingTag: String get() = "<!>"

    fun asString(): String
}
