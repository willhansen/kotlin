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

@Ann(
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!><!UNRESOLVED_REFERENCE!>Nested<!>::class<!>,
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!><!UNRESOLVED_REFERENCE!>Inner<!>::class<!>,
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!><!UNRESOLVED_REFERENCE!>Interface<!>::class<!>,
        <!UNRESOLVED_REFERENCE!>CONST<!>,
        <!UNRESOLVED_REFERENCE!>Companion<!>.CONST,
        <!UNRESOLVED_REFERENCE!>Nested<!>.CONST,
        <!UNRESOLVED_REFERENCE!>Interface<!>.CONST,
        <!UNRESOLVED_REFERENCE!>a<!>,
        <!UNRESOLVED_REFERENCE!>b<!>()
)
class A {

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
