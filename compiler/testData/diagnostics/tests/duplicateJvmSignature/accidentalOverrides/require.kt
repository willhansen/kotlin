open class C {
    konst x = 1
}

interface Tr : <!INTERFACE_WITH_SUPERCLASS!>C<!> {
    <!ACCIDENTAL_OVERRIDE!>fun getX()<!> = 1
}