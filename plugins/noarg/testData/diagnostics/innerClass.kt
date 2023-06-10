annotation class NoArg

class Outer {
    @NoArg
    inner class <!NOARG_ON_INNER_CLASS!>Inner<!>(konst b: Any)
}

fun local() {
    @NoArg
    class <!NOARG_ON_LOCAL_CLASS!>Local<!>(konst l: Any) {
        @NoArg
        inner class <!NOARG_ON_INNER_CLASS!>InnerLocal<!>(konst x: Any)
    }
}
