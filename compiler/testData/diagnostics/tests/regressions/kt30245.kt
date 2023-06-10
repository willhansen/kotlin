// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_ANONYMOUS_PARAMETER -UNUSED_PARAMETER -UNUSED_EXPRESSION

class Sample

fun <K> id(x: K): K = x

fun test() {
    konst f00: Sample.() -> Unit = id { konst a = 1 }
    konst f01: Sample.() -> Unit = id { s: Sample -> }
    konst f02: Sample.() -> Unit = id<Sample.() -> Unit> { s: Sample -> }
}

enum class E { VALUE }

typealias E0 = Int.() -> Int
class W1(konst f: E0) {
    // overload ambiguity is not supported yet - see commented examples with "overload" keyword below
//    constructor(f: () -> Int) : this(fun Int.(): Int = f() )
}

typealias E1 = Int.(String) -> Int
class W2(konst f: E1) {
    // overload ambiguity is not supported yet - see commented examples with "overload" keyword below
//    constructor(f: Int.() -> Int) : this(fun Int.(String): Int = f())
}

typealias L1 = (Int) -> Int
class W3(konst f: L1) {
    // overload ambiguity is not supported yet - see commented examples with "overload" keyword below
//    constructor(f: () -> Int) : this( { i: Int -> f() } )
}

typealias L2 = (Int, String) -> Int
class W4(konst f: L2) {
    // overload ambiguity is not supported yet - see commented examples with "overload" keyword below
//    constructor(f: L1) : this( { i: Int, s: String -> f(i) } )
}

fun test1() { // to extension lambda 0
    konst w10 = W1 { this } // oi+ ni+
    konst i10: E0 = id { this } // o1- ni+
    konst j10 = id<E0> { this } // oi+ ni+
    konst f10 = W1(fun Int.(): Int = this) // oi+ ni+
    konst g10: E0 = id(fun Int.(): Int = this) // oi+ ni+

    konst w11 = W1 <!TYPE_MISMATCH!>{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!>i: Int<!> -> i }<!> // oi- ni-
    konst i11: E0 = id { i: Int -> i } // o1+ ni+
    konst w12 = W1 <!TYPE_MISMATCH!>{ <!CANNOT_INFER_PARAMETER_TYPE, EXPECTED_PARAMETERS_NUMBER_MISMATCH!>i<!> -> <!TYPE_MISMATCH!>i<!> }<!> // oi- ni-
    konst i12: E0 = id <!TYPE_MISMATCH!>{ <!CANNOT_INFER_PARAMETER_TYPE, EXPECTED_PARAMETERS_NUMBER_MISMATCH!>i<!> -> <!TYPE_MISMATCH!>i<!> }<!> // oi- ni-
    konst j12 = id<E0> <!TYPE_MISMATCH!>{ <!CANNOT_INFER_PARAMETER_TYPE, EXPECTED_PARAMETERS_NUMBER_MISMATCH!>i<!> -> <!TYPE_MISMATCH!>i<!> }<!> // oi- ni-

    // yet unsupported cases - considering lambdas as extension ones unconditionally
//    konst w13 = W1 { it } // this or it: oi- ni-
//    konst i13: E0 = id { it } // this or it: oi- ni-
//    konst j13 = id<E0> { it } // this or it: oi- ni-

    konst o14 = W1 { -> 0 } // oi+ ni+
}

fun test2() { // to extension lambda 1
    konst w20 = W2 { this + it.length } // oi+ ni+
    konst i20: E1 = id { this + it.length } // oi- ni+
    konst w21 = W2 { this } // oi+ ni+
    konst i21: E1 = id { this } // oi- ni+
    konst f21 = W2(fun Int.(String): Int = this) // oi+ ni+
    konst g21: E1 = id(fun Int.(String): Int = this) // oi+ ni+
    konst w22 = W2 { s -> this + s.length } // oi+ ni+
    konst i22: E1 = id { s -> this + s.length } // oi+ ni+
    konst w23 = W2 { s -> s.length } // oi+ ni+
    konst i23: E1 = id { s -> s.length } // oi+ ni+
    konst w24 = W2 { s: String -> this + s.length } // oi+ ni+
    konst i24: E1 = id { s: String -> this + s.length } //oi- ni+
    konst w25 = W2 { s: String -> s.length } // oi+ ni+
    konst i25: E1 = id { s: String -> s.length } // oi- ni+
    konst w26 = W2(id { s: String -> this + s.length }) // oi- ni+
    konst w26a = W2(id { s -> this + s.length }) // oi+ ni+
    konst i26: E1 = id { s: String -> this + s.length } // oi- ni+
    konst i26a: E1 = id { s -> this + s.length } // oi+ ni+
    konst e = E.VALUE
    konst w27 = W2(when (e) { E.VALUE ->  { s: String -> this + s.length } }) // oi- ni+
    konst w27a = W2(when (e) { E.VALUE ->  { s -> this + s.length } }) // oi+ ni+
    konst i27: E1 = when (e) { E.VALUE ->  { s: String -> this + s.length } } // oi+ ni+
    konst i27a: E1 = when (e) { E.VALUE ->  { s -> this + s.length } } // oi+ ni+

    konst w28 = W2 <!TYPE_MISMATCH!>{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!><!EXPECTED_PARAMETER_TYPE_MISMATCH, EXPECTED_PARAMETER_TYPE_MISMATCH!>i: Int<!>, <!CANNOT_INFER_PARAMETER_TYPE!>s<!><!> -> i + <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>s<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>length<!> }<!> // oi- ni-
    konst i28: E1 = <!TYPE_MISMATCH!>id <!TYPE_MISMATCH, TYPE_MISMATCH!>{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!><!EXPECTED_PARAMETER_TYPE_MISMATCH, EXPECTED_PARAMETER_TYPE_MISMATCH!>i: Int<!>, <!CANNOT_INFER_PARAMETER_TYPE!>s<!><!> -> i + <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>s<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>length<!> }<!><!> // oi- ni-
    konst w29 = W2 <!TYPE_MISMATCH!>{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!><!EXPECTED_PARAMETER_TYPE_MISMATCH!>i: Int<!>, s: String<!> -> i + s.length }<!> // oi- ni-
    konst i29: E1 = id { i: Int, s: String -> i + s.length } // oi+ ni+

    // yet unsupported cases with ambiguity for the lambda conversion (commented constructors in wrappers above)
//    konst w2a = W2 { i, s -> i + s.length } // overload oi- ni-
//    konst i2a: E1 = id { i, s -> i + s.length } // overload oi- ni-
//    konst w2b = W2 { i, s: String -> i + s.length } // overload oi- ni-
//    konst i2b: E1 = id { i, s: String -> i + s.length } // overload oi- ni-

    // yet unsupported cases with ambiguity for the lambda conversion (commented constructors in wrappers above)
//    konst o2c = W2 { i: Int -> i } // overload oi- ni+
//    konst o2d = W2 { i -> i } // overload oi- ni-
}

fun test3() { // to non-extension lambda 1
    konst w30 = W3 { i -> i } // oi+ ni+
    konst i30: L1 = id { i -> i } // oi+ ni+
    konst w31 = W3 { it } // oi+ ni+
    konst i31: L1 = id { it } // oi- ni+
    konst j31 = id<L1> { it } // oi+ ni+

    // yet unsupported cases - considering lambdas as extension ones unconditionally
//    konst w32 = W3 { this } // this or it: oi- ni-
//    konst i32: L1 = id { this } // this or it: oi- ni-
//    konst j32 = id<L1> { this } // this or it: oi- ni-

    konst w33 = W3(fun Int.(): Int = this) // oi- ni+
    konst i33: L1 = id(fun Int.(): Int = this) // oi+ ni+

    // yet unsupported cases with ambiguity for the lambda conversion (commented constructors in wrappers above)
//    konst o34 = W3 { -> 1 } // overload oi- ni-
}

fun test4() { // to non-extension lambda 2
    konst w30 = W4 { i, s -> i } // oi+ ni+
    konst i30: L2 = id { i, s -> i } // oi+ ni+

    // yet unsupported cases with ambiguity for the lambda conversion (commented constructors in wrappers above)
//    konst w31 = W4 { this } // overload oi- ni-
//    konst i31: L2 = id { this } // overload oi- ni-
//    konst w32 = W4 { this + it.length } // overload oi- ni-
//    konst i32: L2 = id { this + it.length } // overload oi- ni-
}

open class A(a: () -> Unit) {
    constructor(f: (String) -> Unit) : this({ -> f("") })
}

class B: A({ s -> "1" })
