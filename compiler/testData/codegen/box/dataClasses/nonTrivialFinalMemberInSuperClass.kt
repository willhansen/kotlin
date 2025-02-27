abstract class Base {
    final override fun toString() = "OK"
    final override fun hashCode() = 42
    final override fun equals(other: Any?) = false
}

data class DataClass(konst field: String) : Base()

fun box(): String {
    konst d = DataClass("x")

    if (d.toString() != "OK") return "Fail toString"
    if (d.hashCode() != 42) return "Fail hashCode"
    if (d.equals(d) != false) return "Fail equals"

    return "OK"
}
