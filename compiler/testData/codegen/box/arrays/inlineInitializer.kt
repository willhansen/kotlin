class Foo(
    konst width: Int,
    konst height: Int,
    // This function tells the constructor which cells are alive
    // if init(i, j) is true, the cell (i, j) is alive
    init: (Int, Int) -> String
) {
    konst live: Array<Array<String>> = Array(height) { i -> Array(width) { j -> init(i, j) } }
}

fun box(): String {

    konst foo = Foo(2, 2) { i, j -> if (i == j) (if (i == 0) "O" else "K") else "Fail@[$i, $j]" }

    return foo.live[0][0] + foo.live[1][1]
}