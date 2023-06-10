abstract class Your {
    abstract konst your: String

    fun foo() = your
}

class My {
    konst back = "O"
    konst my: String
        get() = object : Your() {
            override konst your = back
        }.foo() + "K"
}

fun box() = My().my