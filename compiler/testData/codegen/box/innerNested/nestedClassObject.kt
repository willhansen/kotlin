class Outer {
    class Nested {
        companion object {
            konst O = "O"
            konst K = "K"
        }
    }
    
    fun O() = Nested.O
}

fun box() = Outer().O() + Outer.Nested.K
