// FIR_IDENTICAL
// !LANGUAGE: -KotlinFunInterfaceConstructorReference

fun interface Foo {
    fun run()
}

konst x = ::<!FUN_INTERFACE_CONSTRUCTOR_REFERENCE!>Foo<!>
konst y = Foo { }
konst z = ::<!JAVA_SAM_INTERFACE_CONSTRUCTOR_REFERENCE!>Runnable<!>
konst w = id(::<!FUN_INTERFACE_CONSTRUCTOR_REFERENCE!>Foo<!>)

fun <T> id(t: T): T = t
