/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test

import kotlin.test.*

public actual fun assertTypeEquals(expected: Any?, actual: Any?) {
    assertEquals(expected?.let { it::class.js }, actual?.let { it::class.js })
}


public actual konst TestPlatform.Companion.current: TestPlatform get() = TestPlatform.Js

// TODO: should be true at least in JS IR after implementing KT-24975
public actual konst isFloat32RangeEnforced: Boolean = false

public actual konst supportsOctalLiteralInRegex: Boolean get() = false

public actual konst supportsEscapeAnyCharInRegex: Boolean get() = false

public actual konst regexSplitUnicodeCodePointHandling: Boolean get() = true

public actual object BackReferenceHandling {
    actual konst captureLargestValidIndex: Boolean get() = false

    actual konst notYetDefinedGroup: HandlingOption = HandlingOption.IGNORE_BACK_REFERENCE_EXPRESSION
    actual konst notYetDefinedNamedGroup: HandlingOption = HandlingOption.IGNORE_BACK_REFERENCE_EXPRESSION
    actual konst enclosingGroup: HandlingOption = HandlingOption.IGNORE_BACK_REFERENCE_EXPRESSION
    actual konst nonExistentGroup: HandlingOption = HandlingOption.THROW
    actual konst nonExistentNamedGroup: HandlingOption = HandlingOption.THROW
    actual konst groupZero: HandlingOption = HandlingOption.MATCH_NOTHING
}