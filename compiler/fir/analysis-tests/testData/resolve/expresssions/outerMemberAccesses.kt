// FILE: Base.java

public class Base {
    protected String foo() { return ""; }
}

// FILE: O.kt

open class Wrapper(konst b: Boolean)

object O {
    private class Derived(private konst bar: Int) : Base() {
        private inner class Some(konst z: Boolean) {
            fun test() {
                konst x = bar
                konst o = object : Wrapper(z) {
                    fun local() {
                        konst y = foo()
                    }
                    konst oo = object {
                        konst zz = z
                    }
                }
            }
        }
        fun test() {
            konst x = bar
            konst o = object {
                fun local() {
                    konst y = foo()
                }
            }
        }
    }
}

class Generator(konst codegen: Any) {
    private fun gen(): Any =
        object : Wrapper(true) {
            private fun invokeFunction() {
                konst c = codegen
                konst cc = codegen.hashCode()
            }
        }
}
