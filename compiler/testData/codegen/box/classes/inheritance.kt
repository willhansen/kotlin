// Changed when traits were introduced. May not make sense any more

open class X(konst x : Int) {}
interface Y {
    abstract konst y : Int
}

class YImpl(override konst y : Int) : Y {}

class Point(x : Int, yy : Int) : X(x) , Y {
    override konst y : Int = yy
}

interface Abstract {}

class P1(x : Int, yy : Y) : Abstract, X(x), Y by yy {}
class P2(x : Int, yy : Y) : X(x), Abstract, Y by yy {}
class P3(x : Int, yy : Y) : X(x), Y by yy, Abstract {}
class P4(x : Int, yy : Y) : Y by yy, Abstract, X(x) {}

fun box() : String {
    if (X(239).x != 239) return "FAIL #1"
    if (YImpl(239).y != 239) return "FAIL #2"

    konst p = Point(240, -1)
    if (p.x + p.y != 239) return "FAIL #3"

    konst y = YImpl(-1)
    konst p1 = P1(240, y)
    if (p1.x + p1.y != 239) return "FAIL #4"
    konst p2 = P2(240, y)
    if (p2.x + p2.y != 239) return "FAIL #5"

    konst p3 = P3(240, y)
    if (p3.x + p3.y != 239) return "FAIL #6"

    konst p4 = P4(240, y)
    if (p4.x + p4.y != 239) return "FAIL #7"

    return "OK"
}
