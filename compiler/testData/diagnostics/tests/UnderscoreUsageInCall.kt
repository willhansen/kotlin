// FIR_IDENTICAL
// !DIAGNOSTICS: -DEPRECATION -TOPLEVEL_TYPEALIASES_ONLY

fun test(`_`: Int) {
    <!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!> + 1
    `_` + 1
}

fun `__`() {}

fun testCall() {
    <!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>__<!>()
    `__`()
}

konst testCallableRef = ::<!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>__<!>
konst testCallableRef2 = ::`__`


object Host {
    konst `_` = 42
    object `__` {
        konst bar = 4
    }
}

konst testQualified = Host.<!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!>
konst testQualified2 = Host.`_`

object `___` {
    konst test = 42
}

konst testQualifier = <!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>___<!>.test
konst testQualifier2 = `___`.test
konst testQualifier3 = Host.<!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>__<!>.bar
konst testQualifier4 = Host.`__`.bar

fun testCallableRefLHSValue(`_`: Any) = <!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>_<!>::toString
fun testCallableRefLHSValue2(`_`: Any) = `_`::toString

konst testCallableRefLHSObject = <!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>___<!>::toString
konst testCallableRefLHSObject2 = `___`::toString
