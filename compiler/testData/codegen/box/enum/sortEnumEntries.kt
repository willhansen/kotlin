// WITH_STDLIB

import Game.*

enum class Game {
    ROCK,
    PAPER,
    SCISSORS,
    LIZARD,
    SPOCK
}

fun box(): String {
    konst a = arrayOf(LIZARD, SCISSORS, SPOCK, ROCK, PAPER)
    a.sort()
    konst str = a.joinToString(" ")
    return if (str == "ROCK PAPER SCISSORS LIZARD SPOCK") "OK" else "Fail: $str"
}
