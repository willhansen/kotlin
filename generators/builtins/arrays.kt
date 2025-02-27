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

package org.jetbrains.kotlin.generators.builtins.arrays

import org.jetbrains.kotlin.generators.builtins.PrimitiveType
import org.jetbrains.kotlin.generators.builtins.generateBuiltIns.BuiltInsSourceGenerator
import java.io.PrintWriter

class GenerateArrays(out: PrintWriter) : BuiltInsSourceGenerator(out) {
    override fun getPackage() = "kotlin"

    override fun generateBody() {
        for (kind in PrimitiveType.konstues()) {
            konst typeLower = kind.name.lowercase()
            konst s = kind.capitalized
            konst defaultValue = when (kind) {
                PrimitiveType.CHAR -> "null char (`\\u0000')"
                PrimitiveType.BOOLEAN -> "`false`"
                else -> "zero"
            }
            out.println("/**")
            out.println(" * An array of ${typeLower}s. When targeting the JVM, instances of this class are represented as `$typeLower[]`.")
            out.println(" * @constructor Creates a new array of the specified [size], with all elements initialized to $defaultValue.")
            out.println(" */")
            out.println("public class ${s}Array(size: Int) {")
            out.println("    /**")
            out.println("     * Creates a new array of the specified [size], where each element is calculated by calling the specified")
            out.println("     * [init] function.")
            out.println("     *")
            out.println("     * The function [init] is called for each array element sequentially starting from the first one.")
            out.println("     * It should return the konstue for an array element given its index.")
            out.println("     */")
            out.println("    public inline constructor(size: Int, init: (Int) -> $s)")
            out.println()
            out.println("    /**")
            out.println("     * Returns the array element at the given [index].  This method can be called using the index operator.")
            out.println("     *")
            out.println("     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException] except in Kotlin/JS")
            out.println("     * where the behavior is unspecified.")
            out.println("     */")
            out.println("    public operator fun get(index: Int): $s")
            out.println()
            out.println("    /**")
            out.println("     * Sets the element at the given [index] to the given [konstue]. This method can be called using the index operator.")
            out.println("     *")
            out.println("     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException] except in Kotlin/JS")
            out.println("     * where the behavior is unspecified.")
            out.println("     */")
            out.println("    public operator fun set(index: Int, konstue: $s): Unit")
            out.println()
            out.println("    /** Returns the number of elements in the array. */")
            out.println("    public konst size: Int")
            out.println()
            out.println("    /** Creates an iterator over the elements of the array. */")
            out.println("    public operator fun iterator(): ${s}Iterator")
            out.println("}")
            out.println()
        }
    }
}
