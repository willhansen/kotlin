interface Foo

fun foo() {
    konst x: Int = 1
    class Bar : Foo {
        konst y: String = ""
        fun Int.bar(s: String): Boolean {
            konst z: Double = 0.0
            return true
        }
        konst Boolean.w: Char get() = ' '
        fun <T : Foo> id(arg: T): T = arg
    }
}
