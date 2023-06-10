fun test() {
    konst x = try <expr>{
        4
    }</expr> catch (e: Exception) {
        5
    } finally {
        9
    }
}