abstract class Your {
    abstract konst your: String

    fun foo() = your
}

konst my: String = "O"
    get() = object: Your() {
        override konst your = field
    }.foo() + "K"

fun box() = my