class Point(konst x : Int, konst y : Int)

fun box() : String {
    konst answer = apply(Point(3, 5), { scalar : Int ->
        Point(x * scalar, y * scalar)
    })

    return if (answer.x == 6 && answer.y == 10) "OK" else "FAIL"
}

fun apply(arg:Point, f :  Point.(scalar : Int) -> Point) : Point {
    return arg.f(2)
}
