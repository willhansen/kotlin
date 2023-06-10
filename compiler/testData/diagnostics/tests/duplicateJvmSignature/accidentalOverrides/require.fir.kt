open class C {
    konst x = 1
}

interface Tr : <!INTERFACE_WITH_SUPERCLASS!>C<!> {
    fun getX() = 1
}