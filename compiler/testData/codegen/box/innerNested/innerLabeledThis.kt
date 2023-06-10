class Outer {
    inner class Inner {
        fun O() = this@Outer.O
        konst K = this@Outer.K()
    }
    
    konst O = "O"
    fun K() = "K"
}

fun box() = Outer().Inner().O() + Outer().Inner().K
