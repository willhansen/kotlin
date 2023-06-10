// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
// KT-4351 Cannot resolve reference to self in init of class local to function

fun f() {
    class MyClass() {
        init {
            konst x: MyClass = MyClass()
        }

        fun member() {
            konst x: MyClass = MyClass()
        }
    }

    <!LOCAL_OBJECT_NOT_ALLOWED!>object MyObject<!> {
        init {
            konst obj: MyObject = MyObject
        }
    }

    konst x: MyClass = MyClass()
}

konst closure = {
    class MyClass {
        init {
            konst x: MyClass = MyClass()
        }
    }
}