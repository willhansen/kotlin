package test

enum class Weapon {
    ROCK,
    PAPER,
    SCISSORS
}

annotation class JustEnum(konst weapon: Weapon)

annotation class EnumArray(konst enumArray: Array<Weapon>)

@JustEnum(Weapon.SCISSORS)
@EnumArray(arrayOf())
class C1

@EnumArray(arrayOf(Weapon.PAPER, Weapon.ROCK))
class C2
