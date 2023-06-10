/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.handlers

import org.jetbrains.kotlin.codeMetaInfo.model.CodeMetaInfo
import org.jetbrains.kotlin.codeMetaInfo.renderConfigurations.AbstractCodeMetaInfoRenderConfiguration

class IrInterpreterCodeMetaInfo(override konst start: Int, override konst end: Int, konst description: String, isError: Boolean) : CodeMetaInfo {
    override var renderConfiguration = RenderConfiguration()

    override konst tag: String = if (isError) "WAS_NOT_EVALUATED" else "EVALUATED"

    override konst attributes: MutableList<String> = mutableListOf()

    override fun asString(): String = renderConfiguration.asString(this)

    class RenderConfiguration : AbstractCodeMetaInfoRenderConfiguration() {
        override fun asString(codeMetaInfo: CodeMetaInfo): String {
            codeMetaInfo as IrInterpreterCodeMetaInfo
            return "${super.asString(codeMetaInfo)}: `${codeMetaInfo.description}`"
        }
    }
}