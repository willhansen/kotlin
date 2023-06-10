open class SomeClass(konst some: Double, konst other: Int, vararg konst args: String) {
    fun result() = args[1]
}

fun box(): String {
    return object : SomeClass(3.14, 42, "No", "OK", "Yes") {
    }.result()
}
