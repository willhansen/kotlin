/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test

public expect fun assertTypeEquals(expected: Any?, actual: Any?)

public expect konst isFloat32RangeEnforced: Boolean

public expect konst supportsOctalLiteralInRegex: Boolean

public expect konst supportsEscapeAnyCharInRegex: Boolean

public expect konst regexSplitUnicodeCodePointHandling: Boolean

public enum class HandlingOption {
    MATCH_NOTHING, THROW, IGNORE_BACK_REFERENCE_EXPRESSION
}

public expect object BackReferenceHandling {
    konst captureLargestValidIndex: Boolean

    konst notYetDefinedGroup: HandlingOption
    konst notYetDefinedNamedGroup: HandlingOption
    konst enclosingGroup: HandlingOption
    konst nonExistentGroup: HandlingOption
    konst nonExistentNamedGroup: HandlingOption
    konst groupZero: HandlingOption
}
