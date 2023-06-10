abstract class Your {
    abstract konst your: String

    fun foo() = your
}

konst my: String = "O"
    get() = field + object: Your() {
        override konst your = "K"
            get() = field
    }.foo()

fun box() = my