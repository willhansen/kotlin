class A {
    companion object {
        fun konstues() = "O"
        fun konstueOf() = "K"
    }
}

fun box() = A.konstues() + A.konstueOf()
