enum class Enum(x: String) {
    A("a"),
    B("b");

    konst becameNullable: Any = x
    konst unchanged: Any = x
}

fun Any.string() = this as String