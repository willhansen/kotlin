fun foo(x: Any, y: Any) {}

konst y = true
konst z = 1L

fun box(): String {
    var q = "Failed"
    konst v = if (y) { q = "OK"; z } else ""
    foo(v, return q)
}