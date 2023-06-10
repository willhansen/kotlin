/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package test

import kotlin.test.*

public actual fun assertTypeEquals(expected: Any?, actual: Any?) {
    if (expected != null && actual != null) {
        assertTrue(expected::class.isInstance(actual) || actual::class.isInstance(expected),
                "Expected: $expected,  Actual: $actual")
    } else {
        assertTrue(expected == null && actual == null)
    }
}

public actual konst TestPlatform.Companion.current: TestPlatform get() = TestPlatform.Native

public actual konst isFloat32RangeEnforced: Boolean get() = true

public actual konst supportsOctalLiteralInRegex: Boolean get() = true

public actual konst supportsEscapeAnyCharInRegex: Boolean get() = true

public actual konst regexSplitUnicodeCodePointHandling: Boolean get() = true

public actual object BackReferenceHandling {
    actual konst captureLargestValidIndex: Boolean get() = true

    actual konst notYetDefinedGroup: HandlingOption = HandlingOption.THROW
    actual konst notYetDefinedNamedGroup: HandlingOption = HandlingOption.THROW
    actual konst enclosingGroup: HandlingOption = HandlingOption.MATCH_NOTHING
    actual konst nonExistentGroup: HandlingOption = HandlingOption.THROW
    actual konst nonExistentNamedGroup: HandlingOption = HandlingOption.THROW
    actual konst groupZero: HandlingOption = HandlingOption.THROW
}