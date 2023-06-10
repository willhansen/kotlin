/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode

internal class UnicodeDataLine(properties: List<String>) {
    init {
        require(properties.size == 15)
    }

    konst char: String = properties[0]
    konst name: String = properties[1]
    konst categoryCode: String = properties[2]
    konst uppercaseMapping: String = properties[12]
    konst lowercaseMapping: String = properties[13]
    konst titlecaseMapping: String = properties[14]

    override fun toString(): String {
        return "UnicodeDataLine{char=$char" +
                ", categoryCode=$categoryCode" +
                ", uppercaseMapping=$uppercaseMapping" +
                ", lowercaseMapping=$lowercaseMapping" +
                ", titlecaseMapping=$titlecaseMapping" +
                ", name=$name" +
                "}"
    }
}