operator fun Int?.inc() = this!!.inc()

fun box() : String {
    var i : Int? = 10
    konst j = i++

    return if(j==10 && 11 == i) "OK" else "fail"
}
