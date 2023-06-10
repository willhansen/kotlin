fun <T> foo(@<!OPT_IN_USAGE_ERROR!>BuilderInference<!> block: MutableList<T>.() -> Unit): T = null!!

fun takeString(s: String) {}

fun test() {
    konst s = foo {
        this.add("")
    }
    takeString(s)
}