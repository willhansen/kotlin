interface I1
interface I2

interface Lazy<T> {
    operator fun getValue(a1: Any, a2: Any): T
}

fun <T> lazy(f: () -> T): Lazy<T> = throw Exception()

class A {
    private inner class B {
        konst o1 = object : I1 {}
        konst o2 by lazy {
            object : I1 {}
        }
        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>konst o3<!> = object : I1, I2 {} // FIR allows this since the containing class is private
        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>konst o4<!> by lazy { // FIR allows this since the containing class is private
            object : I1, I2 {}
        }

        private konst privateO1 = object : I1 {}
        private konst privateO2 by lazy {
            object : I1 {}
        }
        private konst privateO3 = object : I1, I2 {}
        private konst privateO4 by lazy {
            object : I1, I2 {}
        }
    }
}