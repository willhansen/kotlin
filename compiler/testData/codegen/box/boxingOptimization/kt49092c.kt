fun foo(x: Any, y: Any) {}

konst y = false
konst zInt = 1
konst zLong = 1L

fun box(): String {
    var q = "Failed"
    foo(if (y) zInt else { q = "OK"; zLong }, return q)
}