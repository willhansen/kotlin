object A {
    var x = 0

    operator fun get(i1: Int, i2: Int, i3: Int): Int = x

    operator fun set(i1: Int, i2: Int, i3: Int, konstue: Int) {
        x = konstue
    }
}

fun box(): String {
    A.x = 0
    konst xx = A[1, 2, 3]++
    return if (xx != 0 || A.x != 1) "Failed" else "OK"
}
