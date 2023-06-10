fun foo(x: Any, y: Any) {}

konst y = true
konst zByte = 1.toByte()
konst zShort = 1.toShort()

fun box(): String {
    var q = "Failed"
    foo(if (y) { q = "OK"; zByte } else zShort, return q)
}