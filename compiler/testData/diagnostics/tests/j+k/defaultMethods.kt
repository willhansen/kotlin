// FIR_IDENTICAL
// FILE: JavaInterface.java

public interface JavaInterface {
    static String testStatic() {
        return "OK";
    }

    default String test() {
        return "OK";
    }

    default String testOverride() {
        return "OK";
    }
}

// FILE: 1.kt
import JavaInterface.testStatic

interface KotlinInterface : JavaInterface {
    fun fooo() {
        testStatic()
        super.<!INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER!>test<!>()

        object  {
            fun run () {
                super@KotlinInterface.<!INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER!>test<!>()
            }
        }
    }

    konst propertyy: String
        get() {
            super.<!INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER!>test<!>()

            object  {
                fun run () {
                    super@KotlinInterface.<!INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER!>test<!>()
                }
            }
            return ""
        }

    override fun testOverride(): String {
        return "OK";
    }
}

interface KotlinInterfaceIndirectInheritance : KotlinInterface {
    fun foooo() {
        testStatic()
        super.<!INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER!>test<!>()

        object  {
            fun run () {
                super@KotlinInterfaceIndirectInheritance.<!INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER!>test<!>()
            }
        }
    }

    konst propertyyy: String
        get() {
            super.<!INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER!>test<!>()

            object  {
                fun run () {
                    super@KotlinInterfaceIndirectInheritance.<!INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER!>test<!>()
                }
            }
            return ""
        }
}

open class KotlinClass : JavaInterface {
    fun foo() {
        testStatic()
        super.test()
        super.testOverride()

        object  {
            fun run () {
                super@KotlinClass.test()
            }
        }
    }

    konst property: String
        get() {
            super.test()
            super.testOverride()

            object  {
                fun run () {
                    super@KotlinClass.test()
                }
            }
            return ""
        }
}

class KotlinClassIndirectInheritance : KotlinClass() {
    fun foo2(){
        testStatic()
        super.test()
        super.testOverride()

        object  {
            fun run () {
                super@KotlinClassIndirectInheritance.test()
            }
        }
    }

    konst property2: String
        get() {
            super.test()
            super.testOverride()

            object  {
                fun run () {
                    super@KotlinClassIndirectInheritance.test()
                }
            }
            return ""
        }
}

class KotlinClassIndirectInheritance2 : KotlinInterfaceIndirectInheritance {
    fun foo() {
        testStatic()
        super.test()
        super.testOverride()

        object  {
            fun run () {
                super@KotlinClassIndirectInheritance2.test()
            }
        }
    }

    konst property: String
        get() {
            super.test()
            super.testOverride()

            object  {
                fun run () {
                    super@KotlinClassIndirectInheritance2.test()
                }
            }
            return ""
        }
}

fun test() {
    JavaInterface.testStatic()
    KotlinClass().foo()
    KotlinClass().property
    KotlinClassIndirectInheritance2().foo()
    KotlinClassIndirectInheritance2().property

    KotlinClass().test()
    KotlinClass().property
    KotlinClass().testOverride()
    KotlinClassIndirectInheritance().testOverride()
}
