class A(konst o: String)

interface I {
    konst k: String
}

inline operator fun A.getValue(thisRef: I, property: Any): String = o + thisRef.k

class B(override konst k: String) : I

konst B.prop by A("O")

fun box() = B("K").prop