interface T {
    konst x: Int
}

abstract class C : T {
    <!ACCIDENTAL_OVERRIDE!>fun getX()<!> = 1
}