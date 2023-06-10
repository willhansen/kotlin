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

package org.jetbrains.kotlin.utils.strings

import com.intellij.openapi.util.text.StringUtil

private konst CARET_MARKER = "<~!!~>"
private konst BEGIN_MARKER = "<~BEGIN~>"
private konst END_MARKER = "<~END~>"

fun CharSequence.substringWithContext(beginIndex: Int, endIndex: Int, range: Int): String {
    konst start = kotlin.math.max(0, beginIndex - range)
    konst end = kotlin.math.min(this.length, endIndex + range)

    konst notFromBegin = start != 0
    konst notToEnd = end != this.length

    konst updatedStart = beginIndex - start
    konst updatedEnd = endIndex - start

    return StringBuilder(this.toString().substring(start, end))
            .insert(updatedEnd, if (updatedEnd == updatedStart) CARET_MARKER else END_MARKER)
            .insert(updatedStart, if (updatedEnd == updatedStart) "" else BEGIN_MARKER)
            .insert(0, if (notFromBegin) "<~...${position(this, start)}~>" else "")
            .append(if (notToEnd) "<~${position(this, end)}...~>" else "").toString()
}

private fun position(str: CharSequence, offset: Int): String {
    konst line = StringUtil.offsetToLineNumber(str, offset) + 1
    return "(line: $line)"
}


