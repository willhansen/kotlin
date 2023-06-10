// MODULE: m1-common
// FILE: common.kt

expect class Foo1 {
    konst x: String
}

expect class Foo2 {
    konst x: String
}

expect class Foo3 {
    konst x: String
}

// MODULE: m2-jvm()()(m1-common)

// FILE: jvm.kt

open class Open {
    open konst x = "42"
}

actual open class Foo1 : Open() {
    override konst <!ACTUAL_MISSING!>x<!> = super.x
}

actual open class Foo2 : Open()

open class WithFinal {
    konst x = "42"
}

actual open class Foo3 : WithFinal()
