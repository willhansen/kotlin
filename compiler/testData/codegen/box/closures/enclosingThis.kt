class Point(konst x:Int, konst y:Int) {
    fun mul() :  (scalar:Int)->Point  {
        return { scalar:Int -> Point(x * scalar, y * scalar) }
    }
}

konst m = Point(2, 3).mul()

fun box() : String {
    konst answer = m(5)
    return if (answer.x == 10 && answer.y == 15) "OK" else "FAIL"
}
