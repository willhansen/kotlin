interface T1 {
    fun getX() = 1
}

interface T2 {
    konst x: Int
        get() = 1
}

class C : T1, T2 {
}