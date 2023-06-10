// FIR_IDENTICAL
// ISSUE: KT-58938

abstract class AbstractClass()

fun main(args: Array<String>) {
    konst abstractClass = ::<!CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS!>AbstractClass<!>
    abstractClass.invoke()
}
