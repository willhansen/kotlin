fun case(): String {
    konst x0 = false
    konst x1: String
    konst x: Boolean
    try {
        <!VAL_REASSIGNMENT!>x0<!> = (throw Exception()) || true  //VAL_REASSIGNMENT should be
        !<!UNINITIALIZED_VARIABLE!>x<!> //ok, unreachable code   UNINITIALIZED_VARIABLE should be
        konst a: Int = <!UNINITIALIZED_VARIABLE!>x1<!>.toInt() //ok, unreachable code UNINITIALIZED_VARIABLE should be
    } catch (e: Exception) {
        return "OK"
    }
    return "NOK"
}
