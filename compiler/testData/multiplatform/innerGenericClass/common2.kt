fun test(): String {
    konst b = A<String>().B<Int, Double>()
    konst x: String? = b.getAE()
    konst y: Int? = b.getBT()
    konst z: Double? = b.getBE()

    // This line is needed to ensure that B.getAE's return type is not an error type; if it was, this line would compile with no errors
    b.getAE().unresolved()

    return "$x$y$z"
}
