<!DIRECTIVES("HELPERS: REFLECT")!>

open class A {
    konst x1 = true
}

final object <!ELEMENT(2)!> {
    konst x1 = false
}

object <!ELEMENT(1)!> : A() {
    konst x2 = false
}

fun box(): String? {
    if (<!ELEMENT(2)!>.x1) return null
    if (!<!ELEMENT(1)!>.x1 || <!ELEMENT(1)!>.x2) return null

    if (!checkClassName(<!ELEMENT(2)!>::class, "<!ELEMENT_VALIDATION(2)!>")) return null
    if (!checkClassName(<!ELEMENT(1)!>::class, "<!ELEMENT_VALIDATION(1)!>")) return null

    return "OK"
}
