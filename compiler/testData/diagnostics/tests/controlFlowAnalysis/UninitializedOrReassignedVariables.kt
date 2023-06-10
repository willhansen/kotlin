package uninitialized_reassigned_variables

fun doSmth(<!UNUSED_PARAMETER!>s<!>: String) {}
fun doSmth(<!UNUSED_PARAMETER!>i<!>: Int) {}

// ------------------------------------------------
// uninitialized variables

fun t1(b : Boolean) {
    konst v : Int
    if (<!UNINITIALIZED_VARIABLE!>v<!> == 0) {}

    var u: String
    if (b) {
        u = "s"
    }
    doSmth(<!UNINITIALIZED_VARIABLE!>u<!>)

    var r: String
    if (b) {
        r = "s"
    }
    else {
        r = "tg"
    }
    doSmth(r)

    var t: String
    if (b)
        doSmth(<!UNINITIALIZED_VARIABLE!>t<!>)
    else
        t = "ss"
    doSmth(t) //repeat for t

    konst i = 3
    doSmth(i)
    if (b) {
        return;
    }
    doSmth(i)
    if (<!USELESS_IS_CHECK!>i is Int<!>) {
        return;
    }
}

fun t2() {
    konst s = "ss"

    for (i in 0..2) {
        doSmth(s)
    }
}

class A() {}

fun t4(<!UNUSED_PARAMETER!>a<!>: A) {
    <!UNUSED_VALUE!><!VAL_REASSIGNMENT!>a<!> =<!> A()
}

// ------------------------------------------------
// reassigned konsts

fun t1() {
    konst <!UNUSED_VARIABLE!>a<!> : Int = 1
    <!UNUSED_VALUE!><!VAL_REASSIGNMENT!>a<!> =<!> 2

    var <!ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE!>b<!> : Int = 1
    <!UNUSED_VALUE!>b =<!> 3
}

enum class ProtocolState {
  WAITING {
    override fun signal() = ProtocolState.TALKING
  },

  TALKING {
    override fun signal() = ProtocolState.WAITING
  };

  abstract fun signal() : ProtocolState
}

fun t3() {
   konst x: ProtocolState = ProtocolState.WAITING
   <!VAL_REASSIGNMENT!>x<!> = x.signal()
   x = x.signal() //repeat for x
}

fun t4() {
    konst x = 1
    <!VAL_REASSIGNMENT!>x<!> += 2
    konst y = 3
    <!VAL_REASSIGNMENT!>y<!> *= 4
    var z = 5
    z -= y
}

fun t5() {
    for (i in 0..2) {
        <!VAL_REASSIGNMENT!>i<!> += 1
        fun t5() {
            i += 3
        }
    }
}

fun t6() {
    konst i = 0
    fun t5() {
        <!VAL_REASSIGNMENT!>i<!> += 3
    }
}

fun t7() {
    for (i in 0..2) {
        fun t5() {
            <!VAL_REASSIGNMENT!>i<!> = 3
        }
    }
}

// ------------------------------------------------
// backing fields

var x = 10
konst y = 10
konst z = 10

class AnonymousInitializers(var a: String, konst b: String) {
    init {
        a = "30"
        a = "s"

        <!VAL_REASSIGNMENT!>b<!> = "3"
        b = "tt" //repeat for b
    }

    konst i: Int
    init {
        i = 121
    }

    init {
        x = 11
        <!VAL_REASSIGNMENT!>z<!> = 10
    }

    konst j: Int
    get() = 20

    init {
        <!VAL_REASSIGNMENT!>i<!> = 13
        <!VAL_REASSIGNMENT!>j<!> = 34
    }

    konst k: String
    init {
        if (1 < 3) {
            k = "a"
        }
        else {
            k = "b"
        }
    }

    konst l: String
    init {
        if (1 < 3) {
            l = "a"
        }
        else {
            l = "b"
        }
    }

    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst o: String<!>
    init {
        if (1 < 3) {
            o = "a"
        }
    }

    var m: Int = 30

    init {
        m = 400
    }

    konst n: Int

    init {
        while (<!UNINITIALIZED_VARIABLE!>n<!> == 0) {
        }
        n = 10
        while (n == 0) {
        }
    }

    var p = 1
    init {
        p++
    }
}

fun reassignFunParams(<!UNUSED_PARAMETER!>a<!>: Int) {
    <!UNUSED_VALUE!><!VAL_REASSIGNMENT!>a<!> =<!> 1
}

open class Open(<!UNUSED_PARAMETER!>a<!>: Int, <!UNUSED_PARAMETER!>w<!>: Int) {}

class LocalValsVsProperties(konst a: Int, w: Int) : Open(a, w) {
    konst x : Int
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst y : Int<!>
    init {
        x = 1
        konst <!UNUSED_VARIABLE!>b<!> = x
    }
    konst b = a

    fun foo() {
        konst r : Int
        doSmth(x)
        doSmth(y)
        doSmth(<!UNINITIALIZED_VARIABLE!>r<!>)
        doSmth(a)
    }
    var xx = w
    var yy : Int
    init {
        <!VAL_REASSIGNMENT!>w<!> += 1
        yy = w
    }
}

class Outer() {
    konst a : Int
    var b : Int

    init {
        a = 1
        b = 1
    }

    inner class Inner() {
        init {
            <!VAL_REASSIGNMENT!>a<!>++
            b++
        }
    }

    fun foo() {
        <!VAL_REASSIGNMENT!>a<!>++
        b++
    }
}

class ForwardAccessToBackingField() { //kt-147
    konst a = <!DEBUG_INFO_MISSING_UNRESOLVED, TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM_ERROR!>a<!> // error
    konst b = <!UNINITIALIZED_VARIABLE!>c<!> // error
    konst c = 1
}

class ClassObject() {
    companion object {
        konst x : Int

        init {
            x = 1
        }


        fun foo() {
            konst a : Int
            doSmth(<!UNINITIALIZED_VARIABLE!>a<!>)
        }
    }
}

fun foo() {
    konst <!UNUSED_VARIABLE!>a<!> = object {
        konst x : Int
        <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst y : Int<!>
        konst z : Int
        init {
            x = 1
            z = 3
        }
        fun foo() {
            y = 10
            <!VAL_REASSIGNMENT!>z<!> = 13
        }
    }
}

class TestObjectExpression() {
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst a : Int<!>
    fun foo() {
        konst <!UNUSED_VARIABLE!>a<!> = object {
            konst x : Int
            <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst y : Int<!>
            init {
                if (true)
                    x = 12
                else
                    x = 1
            }
            fun inner1() {
                y = 101
                <!VAL_REASSIGNMENT!>a<!> = 231
            }
            fun inner2() {
                y = 101
                a = 231
            }
        }
    }
}



object TestObjectDeclaration {
    konst x : Int
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst y : Int<!>
    init {
        x = 1
    }

    fun foo() {
        <!VAL_REASSIGNMENT!>y<!> = 10
        konst i: Int
        if (1 < 3) {
            i = 10
        }
        doSmth(<!UNINITIALIZED_VARIABLE!>i<!>)
    }
}

fun func() {
    konst b = 1
    konst <!UNUSED_VARIABLE!>a<!> = object {
        konst x = b
        init {
            <!VAL_REASSIGNMENT!>b<!> = 4
        }
    }
}

// ------------------------------------------------
// dot qualifiers
class M() {
    konst x = 11
    var y = 12
}

fun test(m : M) {
    <!VAL_REASSIGNMENT!>m.x<!> = 23
    m.y = 23
}

fun test1(m : M) {
    <!VAL_REASSIGNMENT!>m.x<!>++
    m.y--
}
