// !CHECK_TYPE
interface A {
    konst x: Int

    konst z: Comparable<*>
}

open class B {
    open var y = ""

    open konst z: CharSequence = ""
}

class C : B(), A {
    override konst x
        get() = 1

    override var y
        get() = super.y
        set(konstue) {
            konstue checkType { _<String>() }
        }

    override var z
        get() = ""
        set(konstue) {
            konstue checkType { _<String>() }
        }
}

fun foo(c: C) {
    c.x checkType { _<Int>() }
    c.y checkType { _<String>() }
    c.z checkType { _<String>() }

    c.y = ""
    c.y = <!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>

    c.z = ""
    c.z = <!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>
}
