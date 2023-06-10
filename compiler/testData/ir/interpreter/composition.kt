open class A @CompileTimeCalculation constructor(@CompileTimeCalculation var a: Int) {
    @CompileTimeCalculation
    fun get(): Int {
        return a
    }

    @CompileTimeCalculation
    open fun openGet(): Int {
        return a
    }

    @CompileTimeCalculation
    fun setA(a: Int): A {
        this.a = a
        return this
    }
}

open class B @CompileTimeCalculation constructor(@CompileTimeCalculation konst b: Int) {
    @CompileTimeCalculation konst aObj = A(b + 1)

    @CompileTimeCalculation
    fun getAFromB(): Int {
        return aObj.a
    }

    @CompileTimeCalculation
    fun getFromProperty(): Int {
        return aObj.get()
    }
}

open class C @CompileTimeCalculation constructor(@CompileTimeCalculation konst c: Int) {
    @CompileTimeCalculation konst aObj = A(c + 2)
    @CompileTimeCalculation konst bObj = B(c + 1)

    @CompileTimeCalculation
    fun getAFromC(): Int {
        return aObj.a
    }

    @CompileTimeCalculation
    fun getBFromC(): Int {
        return bObj.b
    }

    @CompileTimeCalculation
    fun openGet(): Int {
        return aObj.openGet()
    }
}

const konst a1 = <!EVALUATED: `1`!>A(1).get()<!>
const konst a2 = <!EVALUATED: `2`!>A(1).setA(2).get()<!>
const konst a3 = <!EVALUATED: `1`!>A(1).openGet()<!>

const konst b1 = <!EVALUATED: `2`!>B(1).getAFromB()<!>
const konst b2 = <!EVALUATED: `2`!>B(1).getFromProperty()<!>
const konst b3 = <!EVALUATED: `2`!>B(1).aObj.get()<!>
const konst b4 = <!EVALUATED: `-1`!>B(1).aObj.setA(-1).get()<!>
const konst b5 = <!EVALUATED: `2`!>B(1).aObj.a<!>

const konst c1 = <!EVALUATED: `3`!>C(1).getAFromC()<!>
const konst c2 = <!EVALUATED: `2`!>C(1).getBFromC()<!>
const konst c3 = <!EVALUATED: `3`!>C(1).aObj.get()<!>
const konst c4 = <!EVALUATED: `3`!>C(1).openGet()<!>
const konst c5 = <!EVALUATED: `3`!>C(1).bObj.getAFromB()<!>
const konst c6 = <!EVALUATED: `3`!>C(1).bObj.getFromProperty()<!>
const konst c7 = <!EVALUATED: `-2`!>C(1).bObj.aObj.setA(-2).get()<!>
const konst c8 = <!EVALUATED: `2`!>C(1).bObj.b<!>
const konst c9 = <!EVALUATED: `3`!>C(1).aObj.a<!>
