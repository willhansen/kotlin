fun Int.thisRef1() = fun () = this
fun Int.thisRef2() = fun (): Int {return this}

fun <T> T.genericThisRef1() = fun () = this
fun <T> T.genericThisRef2() = fun (): T {return this}

konst Int.konstThisRef1: () -> Int get() = fun () = this
konst Int.konstThisRef2: () -> Int get() = fun (): Int {return this}

konst <T> T.konstGenericThisRef1: ()->T get() = fun () = this
konst <T> T.konstGenericThisRef2: ()->T get() = fun (): T {return this}

konst <T> T.withLabel1: ()->T get() = fun () = this@withLabel1
konst <T> T.withLabel2: ()->T get() = fun (): T {return this@withLabel2}

fun box(): String {
    if (1.thisRef1()() != 1) return "Test 1 failed"
    if (2.thisRef2()() != 2) return "Test 2 failed"

    if (3.genericThisRef1()() != 3) return "Test 3 failed"
    if (4.genericThisRef2()() != 4) return "Test 4 failed"

    if (5.konstThisRef1() != 5) return "Test 5 failed"
    if (6.konstThisRef2() != 6) return "Test 6 failed"

    if (7.konstGenericThisRef1() != 7) return "Test 7 failed"
    if (8.konstGenericThisRef2() != 8) return "Test 8 failed"

    if ("bar".withLabel1() != "bar") return "Test 9 failed"
    if ("bar".withLabel2() != "bar") return "Test 10 failed"

    return "OK"
}
