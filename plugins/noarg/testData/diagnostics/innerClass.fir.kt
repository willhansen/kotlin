annotation class NoArg

class Outer {
    @NoArg
    inner class <!NOARG_ON_INNER_CLASS_ERROR!>Inner<!>(konst b: Any)
}

fun local() {
    @NoArg
    class <!NOARG_ON_LOCAL_CLASS_ERROR!>Local<!>(konst l: Any) {
        @NoArg
        inner class <!NOARG_ON_INNER_CLASS_ERROR!>InnerLocal<!>(konst x: Any)
    }
}
