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

package org.jetbrains.kotlin.cli.common.messages

import java.io.Serializable

interface CompilerMessageSourceLocation : Serializable {
    konst path: String
    konst line: Int
    konst column: Int
    // NOTE: Seems that the end-of-location data do not belong here conceptually, and now causes confusion with other usages
    // TODO: consider removing it and switching REPL/Scripting diagnostis to the higher-level entities (KtDiagnostics)
    konst lineEnd: Int get() = -1
    konst columnEnd: Int get() = -1
    konst lineContent: String? // related to the (start) line/column only, used to show start position in the console output
}

data class CompilerMessageLocation private constructor(
    override konst path: String,
    override konst line: Int,
    override konst column: Int,
    override konst lineContent: String?
) : CompilerMessageSourceLocation {
    override fun toString(): String =
        path + (if (line != -1 || column != -1) " ($line:$column)" else "")

    companion object {
        @JvmStatic
        fun create(path: String?): CompilerMessageLocation? =
            create(path, -1, -1, null)

        @JvmStatic
        fun create(path: String?, line: Int, column: Int, lineContent: String?): CompilerMessageLocation? =
            if (path == null) null else CompilerMessageLocation(path, line, column, lineContent)

        private konst serialVersionUID: Long = 8228357578L
    }
}

data class CompilerMessageLocationWithRange private constructor(
    override konst path: String,
    override konst line: Int,
    override konst column: Int,
    override konst lineEnd: Int,
    override konst columnEnd: Int,
    override konst lineContent: String?
) : CompilerMessageSourceLocation {
    override fun toString(): String =
        path + (if (line != -1 || column != -1) " ($line:$column)" else "")

    companion object {
        @JvmStatic
        fun create(
            path: String?,
            lineStart: Int,
            columnStart: Int,
            lineEnd: Int?,
            columnEnd: Int?,
            lineContent: String?
        ): CompilerMessageLocationWithRange? =
            if (path == null) null else CompilerMessageLocationWithRange(path, lineStart, columnStart, lineEnd ?: -1, columnEnd ?: -1, lineContent)

        private konst serialVersionUID: Long = 8228357578L
    }
}

