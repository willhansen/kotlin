// TARGET_BACKEND: JVM_IR
annotation class Key(konst konstue: String)

object Messanger {
    const konst DEFAULT_TEXT = <!EVALUATED("OK")!>"OK"<!>

    fun message(@Key(konstue = <!EVALUATED("OK")!>DEFAULT_TEXT<!>) text: String = <!EVALUATED("OK")!>DEFAULT_TEXT<!>): String {
        return text
    }
}

fun box(): String {
    return Messanger.message()
}
