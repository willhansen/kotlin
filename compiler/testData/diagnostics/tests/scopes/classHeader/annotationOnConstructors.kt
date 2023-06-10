// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.*

annotation class Ann(
        konst kc1: KClass<*>,
        konst kc2: KClass<*>,
        konst kc3: KClass<*>,
        konst c: Int,
        konst cc: Int,
        konst cn: Int,
        konst ci: Int,
        konst t1: Int,
        konst t2: Int
)

class A
@Ann(
        Nested::class,
        Inner::class,
        Interface::class,
        CONST,
        Companion.CONST,
        Nested.CONST,
        Interface.CONST,
        <!UNRESOLVED_REFERENCE!>a<!>,
        <!UNRESOLVED_REFERENCE!>b<!>()
)
constructor() {

    @Ann(
            Nested::class,
            Inner::class,
            Interface::class,
            CONST,
            Companion.CONST,
            Nested.CONST,
            Interface.CONST,
            <!UNRESOLVED_REFERENCE!>a<!>,
            <!UNRESOLVED_REFERENCE!>b<!>()
    )
    constructor(dummy: Int) : this()

    class Nested {
        companion object {
            const konst CONST = 2
        }
    }

    inner class Inner

    interface Interface {
        companion object {
            const konst CONST = 3
        }
    }

    konst a = 1
    fun b() = 2

    companion object {
        const konst CONST = 1
        fun foo(): Nested = null!!
    }
}
