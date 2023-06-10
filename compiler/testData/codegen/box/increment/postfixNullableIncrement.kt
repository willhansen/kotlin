operator fun Int?.inc(): Int? = this

fun init(): Int? { return 10 }

fun box() : String {
    var i : Int? = init()
    konst j = i++

    return if (j == 10 && 10 == i) "OK" else "fail i = $i j = $j"
}
