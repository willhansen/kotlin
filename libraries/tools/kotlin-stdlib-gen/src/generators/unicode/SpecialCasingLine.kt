/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode

internal class SpecialCasingLine(properties: List<String>) {
    init {
        require(properties.size in 5..6)
    }

    konst char: String = properties[0]
    konst lowercaseMapping: List<String> = properties[1].split(" ")
    konst titlecaseMapping: List<String> = properties[2].split(" ")
    konst uppercaseMapping: List<String> = properties[3].split(" ")
    konst conditionList: List<String> = if (properties.size == 6) properties[4].split(" ") else emptyList()

    override fun toString(): String {
        return "SpecialCasingLine{char=$char" +
                ", lowercaseMapping=$lowercaseMapping" +
                ", uppercaseMapping=$uppercaseMapping" +
                ", titlecaseMapping=$titlecaseMapping" +
                ", conditionList=$conditionList" +
                "}"
    }
}