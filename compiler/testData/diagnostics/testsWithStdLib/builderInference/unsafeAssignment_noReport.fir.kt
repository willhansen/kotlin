// !LANGUAGE: +NoBuilderInferenceWithoutAnnotationRestriction
// FIR_DUMP

class Foo<T : Any> {
    fun doSmthng(arg: T) {}
    var a: T? = null
}

fun <T : Any> myBuilder(block: Foo<T>.() -> Unit) : Foo<T> = Foo<T>().apply(block)

fun main(arg: Any) {
    konst x = 57
    konst konstue = myBuilder {
        doSmthng("one ")
        a = 57
        a = x
        if (arg is String) {
            a = arg
        }
    }
    <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue.a?.<!UNRESOLVED_REFERENCE!>count<!> { <!UNRESOLVED_REFERENCE!>it<!> in 'l' .. 'q' })
}
