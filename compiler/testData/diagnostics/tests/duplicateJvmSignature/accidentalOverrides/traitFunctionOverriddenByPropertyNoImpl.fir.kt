interface T {
    fun getX(): Int
}

abstract class C : T {
    konst x: Int
        get() = 1
}