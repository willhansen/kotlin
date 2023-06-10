class Outer private constructor(public konst x: String) {
    class Nested {
        fun foo() = OuterAlias("OK")
    }
}

typealias OuterAlias = Outer

fun box(): String =
        Outer.Nested().foo().x