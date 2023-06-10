@CompileTimeCalculation
interface Object {
    fun get(): String

    @CompileTimeCalculation
    fun defaultGet() = "Object"
}

object A : Object {
    @CompileTimeCalculation
    override fun get() = "A"
}

open class B : Object {
    @CompileTimeCalculation
    override fun get() = "B"
}

object C : B() {
    @CompileTimeCalculation
    override fun get() = "Default: " + super.defaultGet() + "; from super B: " + super.get() + "; from current: " + "companion C"
}

const konst a1 = <!EVALUATED: `Object`!>A.defaultGet()<!>
const konst a2 = <!EVALUATED: `A`!>A.get()<!>
const konst c1 = <!EVALUATED: `Object`!>C.defaultGet()<!>
const konst c2 = <!EVALUATED: `Default: Object; from super B: B; from current: companion C`!>C.get()<!>
