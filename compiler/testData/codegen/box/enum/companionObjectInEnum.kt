enum class Game {
    ROCK,
    PAPER,
    SCISSORS;

    companion object {
        fun foo() = ROCK
        konst bar = PAPER
        konst konstues2 = konstues()
        konst scissors = konstueOf("SCISSORS")
    }
}

fun box(): String {
    if (Game.foo() != Game.ROCK) return "Fail 1"
    if (Game.bar != Game.PAPER) return "Fail 2: ${Game.bar}"
    if (Game.konstues().size != 3) return "Fail 3"
    if (Game.konstueOf("SCISSORS") != Game.SCISSORS) return "Fail 4"
    if (Game.konstues2.size != 3) return "Fail 5"
    if (Game.scissors != Game.SCISSORS) return "Fail 6"
    return "OK"
}
