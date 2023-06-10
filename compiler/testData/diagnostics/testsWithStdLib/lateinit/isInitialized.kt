// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -NOTHING_TO_INLINE
// FILE: test.kt

lateinit var topLevel: String

interface Base {
    var x: String
}

open class Foo : Base {
    override lateinit var x: String
    private lateinit var y: String

    var nonLateInit: Int = 1

    fun ok() {
        ::topLevel.isInitialized
        konst b: Boolean = this::x.isInitialized

        konst otherInstance = Foo()
        otherInstance::x.isInitialized

        (this::x).isInitialized
        (@Suppress("ALL") (this::x)).isInitialized

        object {
            fun local() {
                class Local {
                    konst xx = this@Foo::x.isInitialized
                    konst yy = this@Foo::y.isInitialized
                }
            }
        }
    }

    fun onLiteral() {
        konst p = this::x
        p.<!LATEINIT_INTRINSIC_CALL_ON_NON_LITERAL!>isInitialized<!>
    }

    fun onNonLateinit() {
        this::nonLateInit.<!LATEINIT_INTRINSIC_CALL_ON_NON_LATEINIT!>isInitialized<!>
    }

    inline fun inlineFun() {
        this::x.<!LATEINIT_INTRINSIC_CALL_IN_INLINE_FUNCTION!>isInitialized<!>

        object {
            konst z = this@Foo::x.isInitialized
        }
    }

    inner class InnerSubclass : Foo() {
        fun innerOk() {
            // This is access to Foo.x declared lexically above
            this@Foo::x.isInitialized

            // This is access to InnerSubclass.x which is inherited from Foo.x
            this::x.isInitialized
        }
    }
}

fun onNonAccessible() {
    Foo()::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>isInitialized<!>
}

fun onNonLateinit() {
    Foo()::nonLateInit.<!LATEINIT_INTRINSIC_CALL_ON_NON_LATEINIT!>isInitialized<!>
}

object Unrelated {
    fun onNonAccessible() {
        Foo()::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>isInitialized<!>
    }
}

class FooImpl : Foo() {
    fun onNonAccessible() {
        this::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>isInitialized<!>
    }
}

// FILE: other.kt

class OtherFooImpl : Foo() {
    fun onNonAccessible() {
        this::x.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>isInitialized<!>
        ::topLevel.<!LATEINIT_INTRINSIC_CALL_ON_NON_ACCESSIBLE_PROPERTY!>isInitialized<!>
    }
}
