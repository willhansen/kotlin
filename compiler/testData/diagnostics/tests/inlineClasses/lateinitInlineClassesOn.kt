// WITH_STDLIB
// SKIP_TXT
// !LANGUAGE: +InlineLateinit
// FIR_IDENTICAL

@JvmInline
konstue class IC1(konst x: Int)

@JvmInline
konstue class IC2(konst x: IC1)

@JvmInline
konstue class IC3(konst x: String)

@JvmInline
konstue class IC4(konst x: String?)

@JvmInline
konstue class IC5(konst x: IC4)

@JvmInline
konstue class IC6<T>(konst x: T)

@JvmInline
konstue class IC7<T : Any>(konst x: T)

@JvmInline
konstue class IC8(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>IC9<!>)

@JvmInline
konstue class IC9(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>IC8<!>)

@JvmInline
konstue class IC10(konst x: IC6<String>)

@JvmInline
konstue class IC11(konst x : IC4?)

<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var a: IC1
<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var b: IC2
lateinit var c: IC3
<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var d: IC4
<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var e: IC6<String>
<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var f: IC6<*>
<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var g: IC5
lateinit var h: IC7<Double>
lateinit var i: IC7<*>
lateinit var j: IC8
<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var k : IC10
<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var o : IC3?
<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var m : UInt
<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var n : IC11

class B {
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var a: IC1
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var b: IC2
    lateinit var c: IC3
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var d: IC4
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var e: IC6<String>
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var f: IC6<*>
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var g: IC5
    lateinit var h: IC7<Double>
    lateinit var i: IC7<*>
    lateinit var j: IC8
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var k : IC10
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var o : IC3?
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var m : UInt
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var n : IC11
}

fun foo() {
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var a: IC1
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var b: IC2
    lateinit var c: IC3
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var d: IC4
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var e: IC6<String>
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var f: IC6<*>
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var g: IC5
    lateinit var h: IC7<Double>
    lateinit var i: IC7<*>
    lateinit var j: IC8
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var k : IC10
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var o : IC3?
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var m : UInt
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var n : IC11
}