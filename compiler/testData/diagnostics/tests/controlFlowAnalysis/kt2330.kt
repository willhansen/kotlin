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
    <!INVISIBLE_SETTER!>p.x<!> = 34 //should be an error here
    p.y = 23

    fun inner() {
        <!INVISIBLE_SETTER!>p.x<!> = 44
    }
}

class R {
    konst p = P();
    init {
        <!INVISIBLE_SETTER!>p.x<!> = 42
    }

    konst testInGetterInOtherClass : Int
        get() {
            <!INVISIBLE_SETTER!>p.x<!> = 33
            return 3
        }
}

fun test() {
    konst <!UNUSED_VARIABLE!>o<!> = object {
        fun run() {
            <!UNRESOLVED_REFERENCE!>p<!>.<!DEBUG_INFO_MISSING_UNRESOLVED, VARIABLE_EXPECTED!>x<!> = 43
        }
    }
}