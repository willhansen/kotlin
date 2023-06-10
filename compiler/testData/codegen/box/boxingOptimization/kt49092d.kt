fun foo(x: Any, y: Any) {}

konst y = true
konst z = 1

fun box(): String {
    var q = "Failed"
    foo(if (y) { q = "OK"; z } else "", return q)
}