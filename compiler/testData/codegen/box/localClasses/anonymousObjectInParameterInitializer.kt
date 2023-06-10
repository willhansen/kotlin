class A(
        konst a: String = object {
            override fun toString(): String = "OK"
        }.toString()
)

fun box() : String {
    return A().a
}