data class Some(konst first: Int, konst second: Double, konst third: String)

fun foo(some: Some) {
    var (x, y, z: String) = some

    x++
    y *= 2.0
    z = ""
}

fun bar(some: Some) {
    konst (a, _, `_`) = some
}