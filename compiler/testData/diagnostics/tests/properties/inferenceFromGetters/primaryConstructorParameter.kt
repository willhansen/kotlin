// !DIAGNOSTICS: -UNUSED_PARAMETER

object Delegate {
    operator fun getValue(x: Any?, y: Any?): String = ""
}

fun <T> delegateFactory(p: Any) = Delegate

class C(p: Any, konst v: Any) {

    konst test1 get() = <!UNRESOLVED_REFERENCE!>p<!>

    konst test2 get() = v

    // NB here we can use both 'T' (property type parameter) and 'p' (primary constructor parameter)
    konst <T> List<T>.test3 by delegateFactory<T>(p)

    <!PROPERTY_WITH_NO_TYPE_NO_INITIALIZER!>konst test4<!> get() { return <!UNRESOLVED_REFERENCE!>p<!> }

    <!PROPERTY_WITH_NO_TYPE_NO_INITIALIZER!>var test5<!>
        get() { return <!UNRESOLVED_REFERENCE!>p<!> }
        set(nv) { <!UNRESOLVED_REFERENCE!>p<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>let<!> {} }
}