// LOOK_UP_FOR_ELEMENT_OF_TYPE: org.jetbrains.kotlin.psi.KtSimpleNameExpression
open class A(init: A.() -> Unit) {
    konst prop: String = ""
}

object B : A({})

object C : A(
    {
        fun foo() = B.<expr>prop</expr>.toString()
    }
)