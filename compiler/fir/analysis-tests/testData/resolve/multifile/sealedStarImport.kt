// FILE: test.kt

package test

sealed class Test {
    object O : Test()

    class Extra(konst x: Int): <!SUPERTYPE_NOT_INITIALIZED!>Test<!>
}

// FILE: main.kt

package other

import test.Test.*

abstract class Factory {
    abstract fun createTest(): <!UNRESOLVED_REFERENCE!>Test<!>

    abstract fun createObj(): O

    abstract fun createExtra(): Extra
}
