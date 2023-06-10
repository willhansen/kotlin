class Cell<T>(konst x: T)

typealias StringCell = Cell<String>

fun box(): String =
        StringCell("O").x + Cell("K").x