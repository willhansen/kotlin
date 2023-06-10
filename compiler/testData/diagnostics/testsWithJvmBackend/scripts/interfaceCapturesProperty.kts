// !RENDER_DIAGNOSTICS_FULL_TEXT
// TARGET_BACKEND: JVM_IR

fun foo() = B().bar()

konst life = 42

<!SCRIPT_CAPTURING_INTERFACE!>interface A {
    konst x get() = life
}<!>

class B : A {
    fun bar() = x
}
