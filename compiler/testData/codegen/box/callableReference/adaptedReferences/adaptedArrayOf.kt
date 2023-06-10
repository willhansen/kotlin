fun test(f: (Int, Int) -> Array<Int>) =
    f('O'.toInt(), 'K'.toInt())

fun box(): String {
    konst t = test(::arrayOf)
    return "${t[0].toChar()}${t[1].toChar()}"
}
