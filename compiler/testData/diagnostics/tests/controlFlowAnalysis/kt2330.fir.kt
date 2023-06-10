//KT-2330 Check visibility of getters and setters correspondingly
package a

class P {
    var x : Int = 0
        private set

    var y : Int = 0

    konst other = P();

    init {
        x = 23
        other.x = 4
    }

    konst testInGetter : Int
       get() {
           x = 33
           return 3
       }
}

fun foo() {
    konst p = P()
    p.<!INVISIBLE_SETTER!>x<!> = 34 //should be an error here
    p.y = 23

    fun inner() {
        p.<!INVISIBLE_SETTER!>x<!> = 44
    }
}

class R {
    konst p = P();
    init {
        p.<!INVISIBLE_SETTER!>x<!> = 42
    }

    konst testInGetterInOtherClass : Int
        get() {
            p.<!INVISIBLE_SETTER!>x<!> = 33
            return 3
        }
}

fun test() {
    konst o = object {
        fun run() {
            <!UNRESOLVED_REFERENCE!>p<!>.x = 43
        }
    }
}
