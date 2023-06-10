// !RENDER_DIAGNOSTICS_FULL_TEXT
// TARGET_BACKEND: JVM_IR

fun foo() = B.bar()

konst life = 42

<!SCRIPT_CAPTURING_OBJECT!>object B<!> {
    fun bar() = life
}
