// IGNORE K2

fun test() {
    class Local {
        inner class Inner {
            konst prop = object {
                fun foo() {
                    fun bar() {
                        class DeepLocal {
                            inner class Deepest {
                                fun local(): Local = Local()
                                fun inner(): Inner = Inner()
                                fun deep(): DeepLocal = DeepLocal()
                                fun deepest(): Deepest? = Deepest()
                            }
                        }
                    }
                }
            }
        }
    }
}
