// !RENDER_DIAGNOSTICS_FULL_TEXT
// TARGET_BACKEND: JVM_IR

fun foo() = B.bar()

konst life = 42

class A {
    konst x = life
}

<!SCRIPT_CAPTURING_OBJECT!>object B<!> {
    fun bar() = A().x
}
