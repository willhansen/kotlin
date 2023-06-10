// WITH_STDLIB

package foo

import kotlin.test.assertEquals

enum class TestA {
    FIRST {
        override konst label: String = "first A"
    },
    SECOND {
        override konst label: String = "second A"
    },
    THIRD {
        override konst label: String = "third A"
    };

    abstract konst label: String
}

enum class TestB(konst label: String) {
    FIRST("first B"),
    SECOND("second B"),
    THIRD("third B");
}

fun box(): String {
    assertEquals(TestA.FIRST.label, "first A")
    assertEquals(TestA.SECOND.label, "second A")
    assertEquals(TestA.THIRD.label, "third A")

    assertEquals(TestA.FIRST.ordinal, 0)
    assertEquals(TestA.SECOND.ordinal, 1)
    assertEquals(TestA.THIRD.ordinal, 2)

    assertEquals(TestB.FIRST.label,"first B")
    assertEquals(TestB.SECOND.label,"second B")
    assertEquals(TestB.THIRD.label,"third B")

    assertEquals(TestB.FIRST.ordinal, 0)
    assertEquals(TestB.SECOND.ordinal, 1)
    assertEquals(TestB.THIRD.ordinal, 2)

    return "OK"
}
