fun box(): String {
    try {
        return "OK"
    } finally {
        if (1 == 1) {
            konst z = 2
        }
        if (3 == 3) {
            konst z = 4
        }
    }
}
