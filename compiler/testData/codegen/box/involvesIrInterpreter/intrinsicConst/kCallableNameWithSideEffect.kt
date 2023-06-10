// !LANGUAGE: +IntrinsicConstEkonstuation
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

fun <T> T.id() = this

class A {
    konst a = ""
    fun b() = ""

    init {
        println("A init")
    }

    fun test() {
        konst a = A::a.<!EVALUATED("a")!>name<!>
        konst b = A::b.<!EVALUATED("b")!>name<!>

        konst c = ::A.<!EVALUATED("<init>")!>name<!>
        konst d = this::a.<!EVALUATED("a")!>name<!>

        konst e = A()::b.<!EVALUATED("b")!>name<!>
        konst f = getA()::b.<!EVALUATED("b")!>name<!>

        konst temp = A()
        konst g = temp::b.<!EVALUATED("b")!>name<!>
        konst insideStringConcat = "${temp::b.<!EVALUATED("b")!>name<!>}"

        konst complexExpression1 = A()::a.<!EVALUATED("a")!>name<!> + A()::b.<!EVALUATED("b")!>name<!>
        konst complexExpression2 = A::a.<!EVALUATED("a")!>name<!> <!EVALUATED("ab")!>+ A::b.<!EVALUATED("b")!>name<!><!>

        var recursive = ::test.<!EVALUATED("test")!>name<!>
    }

    fun getA(): A = A()
}

// STOP_EVALUATION_CHECKS
fun box(): String {
    A().test()
    return "OK"
}
