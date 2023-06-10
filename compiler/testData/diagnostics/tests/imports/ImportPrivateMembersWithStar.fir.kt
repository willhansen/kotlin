package test

import test.TopLevelClass.NestedClass.*
import test.TopLevelEnum.NestedEnum.*
import test.TopLevelEnum.*

private class TopLevelClass {
    private class NestedClass {
        class A1
        object A2
    }

    fun test() {
        A1()
        A2
    }
}

private enum class TopLevelEnum(private konst e: NestedEnum) {
    E1(NestedEntry);

    private enum class NestedEnum {
        NestedEntry;
    }
}

fun testAccess() {
    E1
    <!INVISIBLE_REFERENCE!>NestedEntry<!>
    <!INVISIBLE_REFERENCE!>A1<!>()
    <!INVISIBLE_REFERENCE!>A2<!>
}
