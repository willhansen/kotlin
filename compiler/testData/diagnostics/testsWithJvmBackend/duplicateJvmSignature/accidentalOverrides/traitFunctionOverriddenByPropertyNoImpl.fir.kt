interface T {
    fun getX(): Int
}

abstract class C : T {
    <!ACCIDENTAL_OVERRIDE!>konst x: Int<!>
        get() = 1
}
