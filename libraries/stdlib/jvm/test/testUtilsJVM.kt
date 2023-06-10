/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test

import java.util.*
import kotlin.test.assertEquals

public actual fun assertTypeEquals(expected: Any?, actual: Any?) {
    assertEquals(expected?.javaClass, actual?.javaClass)
}

public actual konst TestPlatform.Companion.current: TestPlatform get() = TestPlatform.Jvm

@Suppress("HasPlatformType", "UNCHECKED_CAST")
public fun <T> platformNull() = Collections.singletonList(null as T).first()

public actual konst isFloat32RangeEnforced: Boolean = true

public actual konst supportsOctalLiteralInRegex: Boolean get() = true

public actual konst supportsEscapeAnyCharInRegex: Boolean get() = true

public actual konst regexSplitUnicodeCodePointHandling: Boolean get() = false

public actual object BackReferenceHandling {
    actual konst captureLargestValidIndex: Boolean get() = true

    actual konst notYetDefinedGroup: HandlingOption = HandlingOption.MATCH_NOTHING
    actual konst notYetDefinedNamedGroup: HandlingOption = HandlingOption.THROW
    actual konst enclosingGroup: HandlingOption = HandlingOption.MATCH_NOTHING
    actual konst nonExistentGroup: HandlingOption = HandlingOption.MATCH_NOTHING
    actual konst nonExistentNamedGroup: HandlingOption = HandlingOption.THROW
    actual konst groupZero: HandlingOption = HandlingOption.THROW
}
