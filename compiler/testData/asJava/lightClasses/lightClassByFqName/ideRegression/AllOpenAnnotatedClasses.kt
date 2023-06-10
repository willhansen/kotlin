// test.C
package test

annotation class AllOpen

@AllOpen
class C {
    fun f() {}

    fun g() {}

    konst p: Int

    class D {
        fun z() {

        }
    }

    @AllOpen
    class H {
        fun j() {}
    }
}

// COMPILATION_ERRORS