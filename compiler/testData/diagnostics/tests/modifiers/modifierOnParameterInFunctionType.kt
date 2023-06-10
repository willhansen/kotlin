// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

fun f(vararg x: Int) {}

konst inVal: (<!UNSUPPORTED!>vararg<!> x: Int)->Unit = {}

fun inParam(fn: (<!UNSUPPORTED!>vararg<!> x: Int)->Unit) {}

fun inParamNested(fn1: (fn2: (<!UNSUPPORTED!>vararg<!> n: Int)->Unit)->Unit) {}

fun inReturn(): (<!UNSUPPORTED!>vararg<!> x: Int)->Unit = {}

class A : (<!UNSUPPORTED!>vararg<!> Int)->Unit {
    override fun invoke(p1: Int) {
        var lambda: (<!UNSUPPORTED!>vararg<!> x: Int)->Unit = {}
    }

    konst prop: (<!UNSUPPORTED!>vararg<!> x: Int)->Unit
        get(): (<!UNSUPPORTED!>vararg<!> x: Int)->Unit = {}
}

konst allProhibited: (<!UNSUPPORTED!>abstract<!>
                    <!UNSUPPORTED!>annotation<!>
                    <!UNSUPPORTED!>companion<!>
                    <!UNSUPPORTED!>const<!>
                    <!UNSUPPORTED!>crossinline<!>
                    <!UNSUPPORTED!>data<!>
                    <!UNSUPPORTED!>enum<!>
                    <!UNSUPPORTED!>external<!>
                    <!UNSUPPORTED!>final<!>
                    <!UNSUPPORTED!>in<!>
                    <!UNSUPPORTED!>inline<!>
                    <!UNSUPPORTED!>inner<!>
                    <!UNSUPPORTED!>internal<!>
                    <!UNSUPPORTED!>lateinit<!>
                    <!UNSUPPORTED!>noinline<!>
                    <!UNSUPPORTED!>open<!>
                    <!UNSUPPORTED!>operator<!>
                    <!UNSUPPORTED!>out<!>
                    <!UNSUPPORTED!>override<!>
                    <!UNSUPPORTED!>private<!>
                    <!UNSUPPORTED!>protected<!>
                    <!UNSUPPORTED!>public<!>
                    <!UNSUPPORTED!>reified<!>
                    <!UNSUPPORTED!>sealed<!>
                    <!UNSUPPORTED!>tailrec<!>
                    <!UNSUPPORTED!>vararg<!>

                    x: Int)->Unit = {}

konst konstProhibited: (<!UNSUPPORTED!>konst<!> x: Int)->Unit = {}
konst varProhibited: (<!UNSUPPORTED!>var<!> x: Int)->Unit = {}
