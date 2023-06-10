// CHECK_BYTECODE_LISTING
// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// IGNORE_INLINER: IR
// LANGUAGE: +ValueClasses

@JvmInline
konstue class DPoint(/*inline */konst x: Double/* = 1.0*/, /*inline */konst y: Double/* = 2.0*/) {
    fun f1(a: Int, b: Int = -1, c: DPoint = DPoint(-2.0, -3.0)) = listOf(this, x, y, a, b, c)
    
    constructor(x: Double, flip: Boolean = false): this(x, if (flip) -x else x)
    
    companion object {
        inline operator fun invoke(): DPoint = DPoint(0.0, 0.0)
    }
}

object RegularObject {
    fun pointToString(x: DPoint? = DPoint()) = "$x"
}

@JvmInline
konstue class DSegment(/*inline */konst p1: DPoint/* = DPoint(3.0, 4.0)*/, /*inline */konst p2: DPoint/* = DPoint(5.0, 6.0)*/, /*inline */konst n: Int/* = 7*/) {
    fun f2(a: Int, b: Int = -1, c: DPoint = DPoint(-2.0, -3.0)) = listOf(this, p1, p2, n, a, b, c)
}

data class Wrapper(konst segment: DSegment = DSegment(DPoint(8.0, 9.0), DPoint(10.0, 11.0), 7), konst n: Int = 12) {
    fun f3(a: Int, b: Int = -1, c: DPoint = DPoint(-2.0, -3.0)) = listOf(this, segment, n, a, b, c)
}

fun complexFun(a1: Double, a2: DPoint, a3: Double = a1 * a2.x * a2.y, a4: DPoint = DPoint(a2.x * a1 * a3, a2.y * a1 * a3)) = "$a1, $a2, $a3, $a4"

inline fun complexInlineFun(a1: Double, a2: DPoint, a3: Double = a1 * a2.x * a2.y, a4: DPoint = DPoint(a2.x * a1 * a3, a2.y * a1 * a3)) = "$a1, $a2, $a3, $a4"

fun getLineIntersectionPoint(out: DPoint = DPoint()): DPoint? {
    return getIntersectXY(out)
}

fun getIntersectXY(out: DPoint = DPoint()): DPoint? {
    return out
}

fun box(): String {
//    comments bellow are because MFVC primary constructors default parameters require support of inline arguments in regular functions
//    require(DPoint() == DPoint(1.0, 2.0)) { "${DPoint()} ${DPoint(1.0, 2.0)}" }
//    require(DPoint(3.0) == DPoint(3.0, 2.0)) { "${DPoint()} ${DPoint(3.0, 2.0)}" }
//    require(DPoint(x = 3.0) == DPoint(3.0, 2.0)) { "${DPoint()} ${DPoint(3.0, 2.0)}" }
//    require(DPoint(y = 3.0) == DPoint(1.0, 3.0)) { "${DPoint()} ${DPoint(1.0, 3.0)}" }
//    konst defaultDPoint = DPoint()
    konst defaultDPoint = DPoint(1.0, 2.0)
    require(defaultDPoint.f1(4) == listOf(DPoint(1.0, 2.0), 1.0, 2.0, 4, -1, DPoint(-2.0, -3.0))) {
        defaultDPoint.f1(4).toString()
    }
    require(defaultDPoint.f1(4, 1, DPoint(2.0, 3.0)) == listOf(DPoint(1.0, 2.0), 1.0, 2.0, 4, 1, DPoint(2.0, 3.0))) {
        defaultDPoint.f1(4, 1, DPoint(2.0, 3.0)).toString()
    }
    require(DPoint(-1.0, -2.0).f1(4) == listOf(DPoint(-1.0, -2.0), -1.0, -2.0, 4, -1, DPoint(-2.0, -3.0))) {
        defaultDPoint.f1(4).toString()
    }
    require(DPoint(-1.0, -2.0).f1(4, 1, DPoint(2.0, 3.0)) == listOf(DPoint(-1.0, -2.0), -1.0, -2.0, 4, 1, DPoint(2.0, 3.0))) {
        defaultDPoint.f1(4, 1, DPoint(2.0, 3.0)).toString()
    }

//    require(DSegment() == DSegment(DPoint(3.0, 4.0), DPoint(5.0, 6.0), 7)) { DSegment().toString() }
//    konst defaultDSegment = DSegment()
    konst defaultDSegment = DSegment(DPoint(3.0, 4.0), DPoint(5.0, 6.0), 7)
    require(defaultDSegment.f2(100) == listOf(defaultDSegment, DPoint(3.0, 4.0), DPoint(5.0, 6.0), 7, 100, -1, DPoint(-2.0, -3.0))) {
        defaultDSegment.f2(100).toString()
    }
    require(defaultDSegment.f2(100, b = 1) == listOf(defaultDSegment, DPoint(3.0, 4.0), DPoint(5.0, 6.0), 7, 100, 1, DPoint(-2.0, -3.0))) {
        defaultDSegment.f2(100, b = 1).toString()
    }

    require(Wrapper() == Wrapper(DSegment(DPoint(8.0, 9.0), DPoint(10.0, 11.0), 7), 12)) { Wrapper().toString() }
    require(Wrapper().f3(100) == listOf(Wrapper(), DSegment(DPoint(8.0, 9.0), DPoint(10.0, 11.0), 7), 12, 100, -1, DPoint(-2.0, -3.0))) {
        Wrapper().f3(100).toString()
    }
    require(Wrapper().f3(100, b = 1) == listOf(Wrapper(), DSegment(DPoint(8.0, 9.0), DPoint(10.0, 11.0), 7), 12, 100, 1, DPoint(-2.0, -3.0))) {
        Wrapper().f3(100, b = 1).toString()
    }

    require(complexFun(2.0, DPoint(3.0, 5.0)) == "2.0, ${DPoint(3.0, 5.0)}, 30.0, ${DPoint(180.0, 300.0)}") {
        complexFun(2.0, DPoint(3.0, 5.0))
    }
    require(complexFun(2.0, DPoint(3.0, 5.0), 7.0) == "2.0, ${DPoint(3.0, 5.0)}, 7.0, ${DPoint(42.0, 70.0)}") {
        complexFun(2.0, DPoint(3.0, 5.0), 7.0)
    }
    require(complexFun(2.0, DPoint(3.0, 5.0), a4 = DPoint(11.0, 13.0)) == "2.0, ${DPoint(3.0, 5.0)}, 30.0, ${DPoint(11.0, 13.0)}") {
        complexFun(2.0, DPoint(3.0, 5.0), a4 = DPoint(11.0, 13.0))
    }
    require(complexFun(2.0, DPoint(3.0, 5.0), 7.0, DPoint(11.0, 13.0)) == "2.0, ${DPoint(3.0, 5.0)}, 7.0, ${DPoint(11.0, 13.0)}") {
        complexFun(2.0, DPoint(3.0, 5.0), 7.0, DPoint(11.0, 13.0))
    }

    require(complexInlineFun(2.0, DPoint(3.0, 5.0)) == "2.0, ${DPoint(3.0, 5.0)}, 30.0, ${DPoint(180.0, 300.0)}") {
        complexInlineFun(2.0, DPoint(3.0, 5.0))
    }
    require(complexInlineFun(2.0, DPoint(3.0, 5.0), 7.0) == "2.0, ${DPoint(3.0, 5.0)}, 7.0, ${DPoint(42.0, 70.0)}") {
        complexInlineFun(2.0, DPoint(3.0, 5.0), 7.0)
    }
    require(complexInlineFun(2.0, DPoint(3.0, 5.0), a4 = DPoint(11.0, 13.0)) == "2.0, ${DPoint(3.0, 5.0)}, 30.0, ${DPoint(11.0, 13.0)}") {
        complexInlineFun(2.0, DPoint(3.0, 5.0), a4 = DPoint(11.0, 13.0))
    }
    require(complexInlineFun(2.0, DPoint(3.0, 5.0), 7.0, DPoint(11.0, 13.0)) == "2.0, ${DPoint(3.0, 5.0)}, 7.0, ${DPoint(11.0, 13.0)}") {
        complexInlineFun(2.0, DPoint(3.0, 5.0), 7.0, DPoint(11.0, 13.0))
    }
    
    require(RegularObject.pointToString() == "DPoint(x=0.0, y=0.0)") { RegularObject.pointToString() }
    require(getLineIntersectionPoint().toString() == "DPoint(x=0.0, y=0.0)") { getLineIntersectionPoint().toString() }
    
    require(DPoint(1.0) == DPoint(1.0, 1.0)) { DPoint(1.0).toString() }
    require(DPoint(1.0, flip = false) == DPoint(1.0, 1.0)) { DPoint(1.0, flip = false).toString() }
    require(DPoint(1.0, flip = true) == DPoint(1.0, -1.0)) { DPoint(1.0, flip = true).toString() }
    
    return "OK"
}
