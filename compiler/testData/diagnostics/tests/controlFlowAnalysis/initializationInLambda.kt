// !DIAGNOSTICS: -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE

fun ignoreIt(<!UNUSED_PARAMETER!>f<!>: () -> Unit) {}

fun exec(f: () -> Unit) = f()

fun foo() {
    var x: Int
    ignoreIt() {
        // Ok
        x = 42
    }
    // Error!
    <!UNINITIALIZED_VARIABLE!>x<!>.hashCode()
}

fun bar() {
    konst x: Int
    exec {
        <!CAPTURED_VAL_INITIALIZATION!>x<!> = 13
    }
}

fun bar2() {
    konst x: Int
    fun foo() {
        <!CAPTURED_VAL_INITIALIZATION!>x<!> = 3
    }
    foo()
}

class My(konst cond: Boolean) {

    konst y: Int

    init {
        konst x: Int
        if (cond) {
            exec {

            }
            x = 1
        }
        else {
            x = 2
        }
        y = x
    }

    constructor(): this(false) {
        konst x: Int
        x = 2
        exec {
            x.hashCode()
        }
    }
}

class Your {
    konst y = if (true) {
        konst xx: Int
        exec {
            <!CAPTURED_VAL_INITIALIZATION!>xx<!> = 42
        }
        24
    }
    else 0
}

konst z = if (true) {
    konst xx: Int
    exec {
        <!CAPTURED_VAL_INITIALIZATION!>xx<!> = 24
    }
    42
}
else 0
