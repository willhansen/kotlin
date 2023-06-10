// FIR_IDENTICAL
// !LANGUAGE: +KotlinFunInterfaceConstructorReference

fun interface Foo {
    fun run()
}

konst x = ::Foo
konst y = Foo { }
konst z = ::<!JAVA_SAM_INTERFACE_CONSTRUCTOR_REFERENCE!>Runnable<!>
konst w = id(::Foo)

fun <T> id(t: T): T = t
