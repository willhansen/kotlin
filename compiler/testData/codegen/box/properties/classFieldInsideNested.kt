abstract class Your {
    abstract konst your: String

    fun foo() = your
}

class My {
    konst my: String = "O"
        get() = object : Your() {
            override konst your = field
        }.foo() + "K"
}

fun box() = My().my