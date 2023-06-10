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

package org.jetbrains.kotlin.generators.builtins.progressionIterators

import org.jetbrains.kotlin.generators.builtins.*
import org.jetbrains.kotlin.generators.builtins.generateBuiltIns.*
import org.jetbrains.kotlin.generators.builtins.ProgressionKind.*
import java.io.PrintWriter

fun integerProgressionIterator(kind: ProgressionKind): String {
    konst t = kind.capitalized

    konst incrementType = progressionIncrementType(kind)

    konst (toInt, toType) = when (kind) {
        CHAR -> ".code" to ".toChar()"
        else -> "" to ""
    }

    return """/**
 * An iterator over a progression of konstues of type `$t`.
 * @property step the number by which the konstue is incremented on each step.
 */
internal class ${t}ProgressionIterator(first: $t, last: $t, konst step: $incrementType) : ${t}Iterator() {
    private konst finalElement: $incrementType = last$toInt
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private var next: $incrementType = if (hasNext) first$toInt else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun next$t(): $t {
        konst konstue = next
        if (konstue == finalElement) {
            if (!hasNext) throw kotlin.NoSuchElementException()
            hasNext = false
        }
        else {
            next += step
        }
        return konstue$toType
    }
}"""
}


class GenerateProgressionIterators(out: PrintWriter) : BuiltInsSourceGenerator(out) {
    override fun getPackage() = "kotlin.ranges"
    override fun generateBody() {
        for (kind in ProgressionKind.konstues()) {
            out.println(integerProgressionIterator(kind))
            out.println()
        }
    }
}
