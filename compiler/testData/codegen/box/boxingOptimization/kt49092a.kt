fun foo(x: Any, y: Any) {}

konst y = true
konst z = 1L

fun box(): String {
    var q = "Failed"
    foo(if (y) { q = "OK"; z } else "", return q)
}