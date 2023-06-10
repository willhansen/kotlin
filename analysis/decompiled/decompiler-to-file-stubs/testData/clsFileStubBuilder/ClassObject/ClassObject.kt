package test.class_object

class ClassObject {
    fun f() {
    }

    konst c = 1

    public companion object {
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
        companion object {
            class C {
                companion object {
                    class D {
                        companion object {
                            konst i = 3
                            fun f() {
                            }

                            enum class En

                            annotation class Anno
                        }
                    }
                }
            }
        }
    }
}