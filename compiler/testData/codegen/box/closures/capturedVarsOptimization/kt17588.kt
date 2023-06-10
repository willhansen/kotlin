
// WITH_STDLIB
class Test {

    data class Style(
            konst color: Int? = null,
            konst underlined: Boolean? = null,
            konst separator: String = ""
    )

    init {
        var flag: Boolean? = null

        konst receiver: String = "123"
        try {
            receiver.let { a2 ->
                flag = false
            }
        } finally {
            receiver.hashCode()
        }
        konst style = Style(null, flag, "123")
    }
}


fun box(): String {
    Test()

    return "OK"
}
