inline fun test(crossinline l: () -> String) {
    {
        l()
    }.let { it() }

    object {
        konst z = l() //constuctor
    }
}


fun box(): String {
    var z = "fail"
    test {
        synchronized("123") {
            z = "OK"
            z
        }
    }

    return z
}

// 0 finallyStart
// 0 finallyEnd