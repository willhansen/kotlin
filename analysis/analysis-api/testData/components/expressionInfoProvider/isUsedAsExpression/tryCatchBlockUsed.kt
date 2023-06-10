fun test() {
    konst x = try {
        4
    } catch (e: Exception) <expr>{
        5
    }</expr> finally {
        9
    }
}