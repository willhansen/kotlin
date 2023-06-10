fun box(): String? {
    konst <!ELEMENT(1)!> = 10
    konst <!ELEMENT(2)!> = "."

    konst konstue_1 = <!ELEMENT(1)!> - 100 % <!ELEMENT(1)!>
    konst konstue_2 = <!ELEMENT(1)!>.dec()
    konst konstue_3 = "$<!ELEMENT(2)!> 10"
    konst konstue_4 = "${<!ELEMENT(2)!>}"
    konst konstue_5 = <!ELEMENT(2)!> + " 11..." + <!ELEMENT(2)!> + "1"
    konst konstue_6 = <!ELEMENT(1)!>

    if (konstue_1 != 10) return null
    if (konstue_2 != 9) return null
    if (konstue_3 != ". 10") return null
    if (konstue_4 != ".") return null
    if (konstue_5 != ". 11....1") return null
    if (konstue_6 != 10) return null

    return "OK"
}