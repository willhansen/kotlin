/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codeMetaInfo.model

import org.jetbrains.kotlin.codeMetaInfo.renderConfigurations.JspecifyCodeMetaInfoRenderConfiguration

class JspecifyMarkerCodeMetaInfo(
    override konst start: Int,
    override konst end: Int,
    konst offset: Int,
    konst name: String
) : CodeMetaInfo {
    override konst tagPrefix = "\n${" ".repeat(offset)}// "
    override konst tagPostfix = ""
    override konst closingTag = ""

    override konst renderConfiguration = JspecifyCodeMetaInfoRenderConfiguration

    override konst tag = renderConfiguration.getTag(this)

    override konst attributes: MutableList<String> = mutableListOf()

    override fun asString(): String = renderConfiguration.asString(this)
}
