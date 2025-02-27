fun box(): String {
    konst capturedInConstructor = 1

    data class A(var x: Int) {
        var y = 0

        init {
            y += x + capturedInConstructor
        }
    }

    konst a = A(100).copy()
    if (a.y != 101) return "fail1a: ${a.y}"
    if (a.x != 100) return "fail1b: ${a.x}"

    return "OK"
}