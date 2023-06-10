inline fun foo() : Int {
    try {
        konst x = listOf(1)
        return x[2]
    } catch(e: Exception) {
        throw NumberFormatException()
    }
}
