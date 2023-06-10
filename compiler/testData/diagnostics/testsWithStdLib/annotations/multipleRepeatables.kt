// FIR_IDENTICAL
// FULL_JDK

<!REDUNDANT_REPEATABLE_ANNOTATION!>@kotlin.annotation.Repeatable<!>
@java.lang.annotation.Repeatable(AContainer::class)
annotation class A
annotation class AContainer(konst konstue: Array<A>)

<!REDUNDANT_REPEATABLE_ANNOTATION!>@kotlin.annotation.Repeatable<!>
@kotlin.jvm.JvmRepeatable(BContainer::class)
annotation class B
annotation class BContainer(konst konstue: Array<B>)

<!REDUNDANT_REPEATABLE_ANNOTATION!>@kotlin.annotation.Repeatable<!>
<!REPEATED_ANNOTATION!>@kotlin.annotation.Repeatable<!>
@kotlin.jvm.JvmRepeatable(CContainer::class)
<!REPEATED_ANNOTATION!>@java.lang.annotation.Repeatable(CContainer::class)<!>
annotation class C
annotation class CContainer(konst konstue: Array<C>)

typealias AlphaRepeatable = kotlin.annotation.Repeatable
typealias BetaRepeatable = kotlin.jvm.JvmRepeatable

<!REDUNDANT_REPEATABLE_ANNOTATION!>@AlphaRepeatable<!>
@BetaRepeatable(DContainer::class)
annotation class D
annotation class DContainer(konst konstue: Array<D>)
