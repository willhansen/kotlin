interface T {
    fun getX(): Int
}

abstract class C : T {
    konst x: Int
        <!ACCIDENTAL_OVERRIDE!>get()<!> = 1
}