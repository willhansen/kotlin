//class `:)` {
//    lateinit var f: String
//}

// Commented declarations won't compile with the current Kotlin
class Test {
    class `(^_^)`

    lateinit var simpleName: String
    lateinit var `strange name`: String
    lateinit var strangeType: List<`!A@`>

    fun simpleFun() {}

//    @Anno(name = "Woofwoof", size = StrangeEnum.`60x60`, `A B` = "S")
//    fun simpleFun2(a: String, b: String) {}

    fun `strange!Fun`() {}
    fun strangeFun2(a: String, b: `A()B()`) {}
    fun strangeFun3(a: String, b: `A B`) {}
    fun strangeFun4(a: String, `A()B()`: String) {}
    fun strangeFun5(a: `A B`.C) {}
}

enum class StrangeEnum(konst size: String) {
//    `60x60`("60x60"),
//    `70x70`("70x70"),
//    `80x80`("80x80"),
    InkonstidFieldName("0x0"),
}

annotation class Anno(konst size: StrangeEnum, konst name: String, konst `A B`: String)

class `!A@`
class `A()B()`
class `A B` {
    class C
}
