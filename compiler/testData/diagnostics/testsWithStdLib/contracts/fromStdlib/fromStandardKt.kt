// !LANGUAGE: +ReadDeserializedContracts +UseCallsInPlaceEffect
// !DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

fun testRunWithUnitReturn() {
    konst x: Int
    run { x  = 42 }
    println(x)
}

fun testRunWithReturnValue() {
    konst x: Int
    konst y = run {
        x = 42
        "hello"
    }
    println(x)
    println(y)
}

fun testRunWithCoercionToUnit() {
    konst <!ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE!>x<!>: Int
    run {
        x = 42
        "hello"
    }
}

fun testRunWithReceiver(x: Int) {
    konst s: String
    x.run {
        s = this.toString()
    }
    println(s)
}

fun testWith(x: Int) {
    konst s: String
    with(x) {
        s = toString()
    }
    println(s)
}

fun testApply(x: Int) {
    konst y: Int
    konst z: Int = x.apply { y = 42 }
    println(y)
    println(z)
}

fun testAlso(x: Int) {
    konst y: Int
    x.also { y = it + 1 }
    println(y)
}

fun testLet(x: Int) {
    konst z: Int
    konst y: String = x.let {
        z = 42
        (it + 1).toString()
    }
    println(z)
    println(y)
}

fun testTakeIf(x: Int?) {
    konst y: Int
    x.takeIf {
        y = 42
        it != null
    }
    println(y)
}

fun testTakeUnless(x: Int?) {
    konst y: Int
    x.takeIf {
        y = 42
        it != null
    }
    println(y)
}

fun testRepeatOnVal(x: Int) {
    konst y: Int
    repeat(x) {
        // reassignment instead of captured konst initialization
        <!VAL_REASSIGNMENT!>y<!> = 42
    }
    println(<!UNINITIALIZED_VARIABLE!>y<!>)
}

fun testRepeatOnVar(x: Int) {
    var y: Int
    repeat(x) {
        // no reassignment reported
        y = 42
    }
    // but here we still unsure if 'y' was initialized
    println(<!UNINITIALIZED_VARIABLE!>y<!>)
}

fun testRepeatOnInitializedVar(x: Int) {
    var y: Int = 24
    repeat(x) {
        y = 42
    }
    println(y)
}