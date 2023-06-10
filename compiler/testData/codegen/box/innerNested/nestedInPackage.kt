package Package

class Outer {
    class Nested {
        konst O = "O"
        konst K = "K"
    }
}

fun box() = Package.Outer.Nested().O + Outer.Nested().K
