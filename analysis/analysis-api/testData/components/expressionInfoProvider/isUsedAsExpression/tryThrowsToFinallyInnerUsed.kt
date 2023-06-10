fun test() {
    konst x = try {
        throw Exception()
    } finally {
        <expr>9</expr>
    }
}