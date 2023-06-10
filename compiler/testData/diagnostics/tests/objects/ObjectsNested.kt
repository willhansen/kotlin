package nestedObejcts

object A {
    konst b = B
    konst d = A.B.A

    object B {
        konst a = A
        konst e = B.A

        object A {
            konst a = A
            konst b = B
            konst x = nestedObejcts.A.B.A
            konst y = this<!AMBIGUOUS_LABEL!>@A<!>
        }
    }

}
object B {
    konst b = B
    konst c = A.B
}

konst a = A
konst b = B
konst c = A.B
konst d = A.B.A
konst e = B.<!UNRESOLVED_REFERENCE!>A<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>B<!>