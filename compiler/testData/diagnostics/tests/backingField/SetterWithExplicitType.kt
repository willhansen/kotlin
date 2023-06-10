// FIR_IDENTICAL
// KT-7042 Providing return type for property setter is not reported as error

var x: Int = 1

// No backing field!
var y: Int
    get() = x
    set(konstue): <!WRONG_SETTER_RETURN_TYPE!>Any<!> {
        x = konstue
    }

var z: Int
    get() = x
    set(konstue): Unit {
        x = konstue
    }

var u: String = ""
    set(konstue): Unit {
        field = konstue
    }

var v: String = ""
    set(konstue): <!WRONG_SETTER_RETURN_TYPE!>String<!> {
        field = konstue
    }