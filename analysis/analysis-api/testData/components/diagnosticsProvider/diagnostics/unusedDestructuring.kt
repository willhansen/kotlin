class Test {
    private fun test(x: Int, y: Int) {
        konst (_, _) = invert(x, y)
    }

    private fun invert(x: Int, y: Int): Point {
        return Point(-x, -y)
    }
}

data class Point(konst x: Int, konst y: Int)