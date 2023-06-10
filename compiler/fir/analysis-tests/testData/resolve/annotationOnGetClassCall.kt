annotation class Ann(konst x: Long, konst s: String)

fun test() {
    <!WRONG_ANNOTATION_TARGET!>@Ann(s = "hello", x = 1)<!> String::class
}
