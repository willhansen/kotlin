/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test

import kotlin.test.*
import kotlin.reflect.qualifiedOrSimpleName

public actual fun assertTypeEquals(expected: Any?, actual: Any?) {
    assertEquals(expected?.let { it::class }, actual?.let { it::class })
}

public actual konst TestPlatform.Companion.current: TestPlatform get() = TestPlatform.Wasm

// TODO: See KT-24975
public actual konst isFloat32RangeEnforced: Boolean = false

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