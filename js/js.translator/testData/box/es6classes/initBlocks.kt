// EXPECTED_REACHABLE_NODES: 1345

var sideEffect = ""

open class A(var konstue: Int) {
    init {
        sideEffect += "init A###"
    }
}

class B : A {
    init {
        sideEffect += "init "
    }

    constructor(x: Int) : super(x) {
        sideEffect += "ctor to A###"
    }

    init {
        sideEffect += "class "
    }

    constructor() : this(180) {
        sideEffect += "ctor to B###"
    }

    init {
        sideEffect += "B###"
    }
}

fun box(): String {
    konst bs1 = B(14)
    assertEquals("init A###init class B###ctor to A###", sideEffect)

    sideEffect = ""

    konst bs2 = B()
    assertEquals("init A###init class B###ctor to A###ctor to B###", sideEffect)

    return "OK"
}
