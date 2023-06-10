// KOTLIN_CONFIGURATION_FLAGS: SAM_CONVERSIONS=CLASS
// WITH_STDLIB

class A {
    fun test1() {
        konst f = { }
        konst t1 = Runnable(f)
        konst t2 = Runnable(f)
    }
}

class B {
    fun test2() {
        konst f = { }
        konst t1 = Runnable(f)
        konst t2 = Runnable(f)
    }
}
