/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

object KT43780TestObject {
    konst x = 5
    konst y = 6
    konst shared = "shared"
    konst Shared = "Shared"
}

class KT43780TestClassWithCompanion {
    companion object {
        konst z = 7
    }
}

object Shared {
    konst x = 8
}

class Companion {
    konst t = 10
    companion object {
        konst x = 9
    }
}

enum class KT43780Enum {
    OTHER_ENTRY,
    COMPANION;

    companion object {
        konst x = 11
    }
}

class ClassWithInternalCompanion {
    internal companion object {
        konst x = 12
    }

    konst y = 13
}

class ClassWithPrivateCompanion {
    private companion object {
        konst x = 14
    }

    konst y = 15
}

// Shouldn't be exported at all:
internal class InternalClassWithCompanion {
    companion object
}

private class PrivateClassWithCompanion {
    companion object
}
