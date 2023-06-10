// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_DESTRUCTURED_PARAMETER_ENTRY, -UNUSED_ANONYMOUS_PARAMETER
// !LANGUAGE: -TrailingCommas

data class Foo1(konst x: String, konst y: String, konst z: String = "")

fun main() {
    konst (x1,y1,) = Pair(1,2)
    konst (x2, y2: Number,) = Pair(1,2)
    konst (x3,y3,z3,) = Foo1("", "", )
    konst (x4,y4: CharSequence,) = Foo1("", "", "",)
    konst (x41,y41: CharSequence,/**/) = Foo1("", "", "",)

    konst x5: (Pair<Int, Int>, Int) -> Unit = { (x,y,),z, -> }
    konst x6: (Foo1, Int) -> Any = { (x,y,z: CharSequence,), z1: Number, -> 1 }
    konst x61: (Foo1, Int) -> Any = { (x,y,z: CharSequence,/**/), z1: Number,/**/ -> 1 }

    for ((i, j,) in listOf(Pair(1,2))) {}
    for ((i: Any,) in listOf(Pair(1,2))) {}
    for ((i: Any,/**/) in listOf(Pair(1,2))) {}
}
