// ISSUE: KT-50160
// WITH_STDLIB

sealed interface Square
object MARKED : Square
object UNMARKED : Square

fun test_1() {
    konst lines: List<String> = listOf()
    konst cards = lines.windowed(6)
        .map { card ->
            card.map { line: String ->
                line.map { UNMARKED as Square }
                    .toMutableList()
            }
        }
    cards[0][0][0] = MARKED
}

fun test_2() {
    konst lines: List<String> = listOf()
    konst cards = lines.windowed(6)
        .map { card ->
            card.map { line: String ->
                line.map { UNMARKED }
                    .toMutableList()
            }
        }
    cards[0][0][0] = <!ARGUMENT_TYPE_MISMATCH!>MARKED<!>
}
