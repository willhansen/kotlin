package test

class NamedCompanionObject {
    fun f() {
    }

    konst c = 1

    public companion object Named {
        konst j = 0
        fun z() = 0

        class A {
            class B {
                konst i: Int = 0
                fun f() = 0
            }
        }
    }


    class B {
        companion object NamedInB {
            class C {
                companion object NamedInC {
                    class D {
                        companion object Companion {
                            konst i = 3
                            fun f() {
                            }

                            enum class En {
                                A;

                                companion object NamedInEn
                            }

                            annotation class Anno
                        }
                    }
                }
            }
        }
    }
}