// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

fun simple() = 1
fun simple(a: Int = 3) = ""

fun twoDefault(a: Int = 2) = 1
fun twoDefault(a: Any = 2, b: String = "") = ""

fun <T> withGeneric(a: T) = 1
fun <T> withGeneric(a: T, b: Int = 4) = ""

fun <T> discriminateGeneric(a: T) = 1
fun discriminateGeneric(a: Int, b: String = "") = ""

fun <T: CharSequence> withDefaultGeneric(t: T, d: T? = null) = 1
fun <T: Any> withDefaultGeneric(t: T, d: T? = null, a: Int = 1) = ""

fun withDefaults(a: Any = 2) = 1
fun withDefaults(a: Int = 2, b: String = "") = ""

fun <T: Any> withGenericDefaults(t: T, d: T? = null) = 1
fun <T: CharSequence> withGenericDefaults(t: T, d: T? = null, a: Int = 1) = ""

fun wrong(a: Int = 1) {}
fun wrong(a: String = "", b: Int = 1) {}

fun test() {
    konst a = simple()
    a checkType { _<Int>() }

    konst b = simple(1)
    b checkType { _<String>() }

    konst c = twoDefault()
    c checkType { _<Int>() }

    konst d = twoDefault(1)
    d checkType { _<Int>() }

    konst e = twoDefault(1, "")
    e checkType { _<String>() }

    konst f = withGeneric(3)
    f checkType { _<Int>() }

    konst g = discriminateGeneric(1)
    g checkType { _<String>() }

    konst h = withDefaultGeneric("")
    h checkType { _<Int>() }

    withDefaults(1)

    withGenericDefaults("")

    <!UNREACHABLE_CODE!><!OVERLOAD_RESOLUTION_AMBIGUITY!>wrong<!>(<!>null!!<!UNREACHABLE_CODE!>)<!>
}