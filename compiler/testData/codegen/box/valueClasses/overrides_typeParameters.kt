// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses
// LANGUAGE: +GenericInlineClassParameter
// CHECK_BYTECODE_LISTING

interface AbstractPoint<T> {
    konst x: T
    konst y: T
}

interface SomeInterface<X> {
    fun <T, R: X> someFunction1(x: X, t: T, r: R) = Unit
    fun <T, R: X> someFunction2(x: XPoint<X>, t: XPoint<T>, r: XPoint<R>) = Unit
    
}

@JvmInline
konstue class XPoint<X>(override konst x: X, override konst y: X): AbstractPoint<X>, SomeInterface<X> {
    override fun <T, R: X> someFunction1(x: X, t: T, r: R) = Unit
    override fun <T, R: X> someFunction2(x: XPoint<X>, t: XPoint<T>, r: XPoint<R>) = Unit
}

@JvmInline
konstue class YPoint<X>(konst x: X)

fun <S: List<Int>> genericFunctionMFVC(v: XPoint<S>) {}
fun <S: List<Int>> genericFunctionIC(v: YPoint<S>) {}

interface GenericMFVCHolder<T> {
    var p: T
    var p1: T
}

interface GenericMFVCHolderWithMFVCUpperBound<X, T : XPoint<X>> {
    var p: T
    var p1: T
}

interface ReifiedMFVCHolder<X> {
    var p: XPoint<X>
    var p1: XPoint<X>
}

data class DataClassException(konst konstue: Any?): Exception()

interface GenericMFVCHolderWithImpls<T> {
    var p: T
        get() = throw DataClassException(1)
        set(konstue) = throw DataClassException(2 to konstue)

    var p1: T
        get() = throw DataClassException(3)
        set(konstue) = throw DataClassException(4 to konstue)
}

interface GenericMFVCHolderWithMFVCUpperBoundWithImpls<X, T : XPoint<X>> {
    var p: T
        get() = throw DataClassException(5)
        set(konstue) = throw DataClassException(6 to konstue)

    var p1: T
        get() = throw DataClassException(7)
        set(konstue) = throw DataClassException(8 to konstue)
}

interface ReifiedMFVCHolderWithImpls<X> {
    var p: XPoint<X>
        get() = throw DataClassException(9)
        set(konstue) = throw DataClassException(10 to konstue)

    var p1: XPoint<X>
        get() = throw DataClassException(11)
        set(konstue) = throw DataClassException(12 to konstue)
}

class RealOverride<X>(override var p: XPoint<X>) : GenericMFVCHolder<XPoint<X>>, ReifiedMFVCHolder<X>, GenericMFVCHolderWithMFVCUpperBound<X, XPoint<X>> {
    override var p1: XPoint<X>
        get() = throw DataClassException(13)
        set(konstue) = throw DataClassException(14 to konstue)
}

class GenericFakeOverride<X> : GenericMFVCHolderWithImpls<XPoint<X>>
class ReifiedFakeOverride<X> : ReifiedMFVCHolderWithImpls<X>
class GenericFakeOverrideWithMFVCUpperBound<X> : GenericMFVCHolderWithMFVCUpperBoundWithImpls<X, XPoint<X>>


@JvmInline
konstue class GenericFakeOverrideMFVC<X>(konst field1: X, konst field2: X) : GenericMFVCHolderWithImpls<XPoint<X>>
@JvmInline
konstue class ReifiedFakeOverrideMFVC<X>(konst field1: X, konst field2: X) : ReifiedMFVCHolderWithImpls<X>
@JvmInline
konstue class GenericFakeOverrideMFVCWithMFVCUpperBound<X>(konst field1: X, konst field2: X) : GenericMFVCHolderWithMFVCUpperBoundWithImpls<X, XPoint<X>>


interface SomePointInterface<X, T> {
    var somethingRegular: Int

    var somethingGeneric: T

    var somethingMFVC: XPoint<X>
}

interface SomePointInterfaceWithMFVCBound<X, T : XPoint<X>> {
    var somethingRegular: Int

    var somethingGeneric: T

    var somethingMFVC: XPoint<X>
}

@JvmInline
konstue class XPointWithInterface<X, Y, Z>(konst x: Y, konst y: Z) : SomePointInterface<X, XPoint<X>>, SomePointInterfaceWithMFVCBound<X, XPoint<X>> {
    override var somethingGeneric: XPoint<X>
        get() = throw DataClassException(15)
        set(konstue) = throw DataClassException(16 to konstue)

    override var somethingMFVC: XPoint<X>
        get() = throw DataClassException(17)
        set(konstue) = throw DataClassException(18 to konstue)

    override var somethingRegular: Int
        get() = throw DataClassException(19)
        set(konstue) = throw DataClassException(20 to konstue)
}


interface AbstractSegment<T> {
    konst p1: T
    konst p2: T
}

@JvmInline
konstue class XSegment<X>(override konst p1: XPoint<X>, override konst p2: XPoint<X>): AbstractSegment<XPoint<X>>

fun <T> equal(expected: () -> T, actual: () -> T) {
    konst expectedResult = runCatching { expected() }
    konst actualResult = runCatching { actual() }
    require(expectedResult == actualResult) { "Expected: $expectedResult\nActual: $actualResult" }
}

fun box(): String {
    konst xPoint = XPoint(1.0, 2.0)

    konst lam1: () -> XPoint<Double> = { throw DataClassException(1) }
    konst lam2: () -> Unit = { throw DataClassException(2 to xPoint) }
    konst lam3: () -> XPoint<Double> = { throw DataClassException(3) }
    konst lam4: () -> Unit = { throw DataClassException(4 to xPoint) }
    konst lam5: () -> XPoint<Double> = { throw DataClassException(5) }
    konst lam6: () -> Unit = { throw DataClassException(6 to xPoint) }
    konst lam7: () -> XPoint<Double> = { throw DataClassException(7) }
    konst lam8: () -> Unit = { throw DataClassException(8 to xPoint) }
    konst lam9: () -> XPoint<Double> = { throw DataClassException(9) }
    konst lam10: () -> Unit = { throw DataClassException(10 to xPoint) }
    konst lam11: () -> XPoint<Double> = { throw DataClassException(11) }
    konst lam12: () -> Unit = { throw DataClassException(12 to xPoint) }
    konst lam13: () -> XPoint<Double> = { throw DataClassException(13) }
    konst lam14: () -> Unit = { throw DataClassException(14 to xPoint) }
    konst lam15: () -> XPoint<Double> = { throw DataClassException(15) }
    konst lam16: () -> Unit = { throw DataClassException(16 to xPoint) }
    konst lam17: () -> XPoint<Double> = { throw DataClassException(17) }
    konst lam18: () -> Unit = { throw DataClassException(18 to xPoint) }
    konst lam19: () -> Int = { throw DataClassException(19) }
    konst lam20: () -> Unit = { throw DataClassException(20 to 1) }
    konst emptyLam = {}
    konst xPointLam = { xPoint }
    konst otherXPoint = XPoint(3.0, 4.0)
    konst otherXPointLam = { otherXPoint }
    equal({ "XPoint(x=1.0, y=2.0)" }, { xPoint.toString() })
    equal({ "XPoint(x=1.0, y=2.0)" }, { (xPoint as Any).toString() })

    equal({ true }, { xPoint.equals(xPoint) })
    equal({ true }, { xPoint.equals(xPoint as Any) })
    equal({ true }, { (xPoint as Any).equals(xPoint) })
    equal({ true }, { (xPoint as Any).equals(xPoint as Any) })

    equal({ false }, { xPoint.equals(otherXPoint) })
    equal({ false }, { xPoint.equals(otherXPoint as Any) })
    equal({ false }, { (xPoint as Any).equals(otherXPoint) })
    equal({ false }, { (xPoint as Any).equals(otherXPoint as Any) })

    equal({ xPoint.hashCode() }, { (xPoint as Any).hashCode() })

    equal({ 1.0 }, { xPoint.x })
    equal({ 1.0 }, { (xPoint as AbstractPoint<Double>).x })
    equal({ 2.0 }, { xPoint.y })
    equal({ 2.0 }, { (xPoint as AbstractPoint<Double>).y })


    konst realOverride = RealOverride(xPoint)

    equal(xPointLam, { realOverride.p })
    equal(xPointLam, { (realOverride as GenericMFVCHolder<XPoint<Double>>).p })
    equal(lam13, { realOverride.p1 })
    equal(lam13, { (realOverride as GenericMFVCHolder<XPoint<Double>>).p1 })
    equal(xPointLam, { (realOverride as ReifiedMFVCHolder<Double>).p })
    equal(lam13, { realOverride.p1 })
    equal(lam13, { (realOverride as ReifiedMFVCHolder<Double>).p1 })
    equal(xPointLam, { (realOverride as GenericMFVCHolderWithMFVCUpperBound<Double, XPoint<Double>>).p })
    equal(lam13, { (realOverride as GenericMFVCHolderWithMFVCUpperBound<Double, XPoint<Double>>).p1 })


    equal(emptyLam, { realOverride.p = xPoint })
    equal(emptyLam, { (realOverride as GenericMFVCHolder<XPoint<Double>>).p = xPoint })
    equal(lam14, { realOverride.p1 = xPoint })
    equal(lam14, { (realOverride as GenericMFVCHolder<XPoint<Double>>).p1 = xPoint })
    equal(emptyLam, { (realOverride as ReifiedMFVCHolder<Double>).p = xPoint })
    equal(lam14, { (realOverride as ReifiedMFVCHolder<Double>).p1 = xPoint })
    equal(emptyLam, { (realOverride as GenericMFVCHolderWithMFVCUpperBound<Double, XPoint<Double>>).p = xPoint })
    equal(lam14, { (realOverride as GenericMFVCHolderWithMFVCUpperBound<Double, XPoint<Double>>).p1 = xPoint })


    konst genericFakeOverride = GenericFakeOverride<Double>()

    equal(lam1, { genericFakeOverride.p })
    equal(lam1, { (genericFakeOverride as GenericMFVCHolderWithImpls<XPoint<Double>>).p })
    equal(lam3, { genericFakeOverride.p1 })
    equal(lam3, { (genericFakeOverride as GenericMFVCHolderWithImpls<XPoint<Double>>).p1 })
    konst reifiedFakeOverride = ReifiedFakeOverride<Double>()
    equal(lam9, { reifiedFakeOverride.p })
    equal(lam9, { (reifiedFakeOverride as ReifiedMFVCHolderWithImpls<Double>).p })
    equal(lam11, { reifiedFakeOverride.p1 })
    equal(lam11, { (reifiedFakeOverride as ReifiedMFVCHolderWithImpls<Double>).p1 })
    konst genericFakeOverrideWithMFVCUpperBound = GenericFakeOverrideWithMFVCUpperBound<Double>()
    equal(lam5, { genericFakeOverrideWithMFVCUpperBound.p })
    equal(lam5, { (genericFakeOverrideWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<Double, XPoint<Double>>).p })
    equal(lam7, { genericFakeOverrideWithMFVCUpperBound.p1 })
    equal(lam7, { (genericFakeOverrideWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<Double, XPoint<Double>>).p1 })

    equal(lam2, { genericFakeOverride.p = xPoint })
    equal(lam2, { (genericFakeOverride as GenericMFVCHolderWithImpls<XPoint<Double>>).p = xPoint })
    equal(lam4, { genericFakeOverride.p1 = xPoint })
    equal(lam4, { (genericFakeOverride as GenericMFVCHolderWithImpls<XPoint<Double>>).p1 = xPoint })
    equal(lam10, { reifiedFakeOverride.p = xPoint })
    equal(lam10, { (reifiedFakeOverride as ReifiedMFVCHolderWithImpls<Double>).p = xPoint })
    equal(lam12, { reifiedFakeOverride.p1 = xPoint })
    equal(lam12, { (reifiedFakeOverride as ReifiedMFVCHolderWithImpls<Double>).p1 = xPoint })
    equal(lam6, { genericFakeOverrideWithMFVCUpperBound.p = xPoint })
    equal(lam6, { (genericFakeOverrideWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<Double, XPoint<Double>>).p = xPoint })
    equal(lam8, { genericFakeOverrideWithMFVCUpperBound.p1 = xPoint })
    equal(lam8, { (genericFakeOverrideWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<Double, XPoint<Double>>).p1 = xPoint })


    konst genericFakeOverrideMFVC = GenericFakeOverrideMFVC(1.0, 2.0)

    equal(lam1, { genericFakeOverrideMFVC.p })
    equal(lam1, { (genericFakeOverrideMFVC as GenericMFVCHolderWithImpls<XPoint<Double>>).p })
    equal(lam3, { genericFakeOverrideMFVC.p1 })
    equal(lam3, { (genericFakeOverrideMFVC as GenericMFVCHolderWithImpls<XPoint<Double>>).p1 })

    konst reifiedFakeOverrideMFVC = ReifiedFakeOverrideMFVC(1.0, 2.0)
    equal(lam9, { reifiedFakeOverrideMFVC.p })
    equal(lam9, { (reifiedFakeOverrideMFVC as ReifiedMFVCHolderWithImpls<Double>).p })
    equal(lam11, { reifiedFakeOverrideMFVC.p1 })
    equal(lam11, { (reifiedFakeOverrideMFVC as ReifiedMFVCHolderWithImpls<Double>).p1 })

    konst genericFakeOverrideMFVCWithMFVCUpperBound = GenericFakeOverrideMFVCWithMFVCUpperBound(1.0, 2.0)
    equal(lam5, { genericFakeOverrideMFVCWithMFVCUpperBound.p })
    equal(lam5, { (genericFakeOverrideMFVCWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<Double, XPoint<Double>>).p })
    equal(lam7, { genericFakeOverrideMFVCWithMFVCUpperBound.p1 })
    equal(lam7, { (genericFakeOverrideMFVCWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<Double, XPoint<Double>>).p1 })

    equal(lam2, { genericFakeOverrideMFVC.p = xPoint })
    equal(lam2, { (genericFakeOverrideMFVC as GenericMFVCHolderWithImpls<XPoint<Double>>).p = xPoint })
    equal(lam4, { genericFakeOverrideMFVC.p1 = xPoint })
    equal(lam4, { (genericFakeOverrideMFVC as GenericMFVCHolderWithImpls<XPoint<Double>>).p1 = xPoint })

    equal(lam10, { reifiedFakeOverrideMFVC.p = xPoint })
    equal(lam10, { (reifiedFakeOverrideMFVC as ReifiedMFVCHolderWithImpls<Double>).p = xPoint })
    equal(lam12, { reifiedFakeOverrideMFVC.p1 = xPoint })
    equal(lam12, { (reifiedFakeOverrideMFVC as ReifiedMFVCHolderWithImpls<Double>).p1 = xPoint })

    equal(lam6, { genericFakeOverrideMFVCWithMFVCUpperBound.p = xPoint })
    equal(lam6, { (genericFakeOverrideMFVCWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<Double, XPoint<Double>>).p = xPoint })
    equal(lam8, { genericFakeOverrideMFVCWithMFVCUpperBound.p1 = xPoint })
    equal(lam8, { (genericFakeOverrideMFVCWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<Double, XPoint<Double>>).p1 = xPoint })


    konst xPointWithInterface = XPointWithInterface<Double, Double, Double>(1.0, 2.0)

    equal(lam15, { xPointWithInterface.somethingGeneric })
    equal(lam15, { (xPointWithInterface as SomePointInterface<Double, XPoint<Double>>).somethingGeneric })
    equal(lam19, { xPointWithInterface.somethingRegular })
    equal(lam19, { (xPointWithInterface as SomePointInterface<Double, XPoint<Double>>).somethingRegular })
    equal(lam17, { xPointWithInterface.somethingMFVC })
    equal(lam17, { (xPointWithInterface as SomePointInterface<Double, XPoint<Double>>).somethingMFVC })

    equal(lam15, { (xPointWithInterface as SomePointInterfaceWithMFVCBound<Double, XPoint<Double>>).somethingGeneric })
    equal(lam19, { (xPointWithInterface as SomePointInterfaceWithMFVCBound<Double, XPoint<Double>>).somethingRegular })
    equal(lam17, { (xPointWithInterface as SomePointInterfaceWithMFVCBound<Double, XPoint<Double>>).somethingMFVC })

    equal(lam16, { xPointWithInterface.somethingGeneric = xPoint })
    equal(lam16, { (xPointWithInterface as SomePointInterface<Double, XPoint<Double>>).somethingGeneric = xPoint })
    equal(lam20, { xPointWithInterface.somethingRegular = 1 })
    equal(lam20, { (xPointWithInterface as SomePointInterface<Double, XPoint<Double>>).somethingRegular = 1 })
    equal(lam18, { xPointWithInterface.somethingMFVC = xPoint })
    equal(lam18, { (xPointWithInterface as SomePointInterface<Double, XPoint<Double>>).somethingMFVC = xPoint })

    equal(lam16, { (xPointWithInterface as SomePointInterfaceWithMFVCBound<Double, XPoint<Double>>).somethingGeneric = xPoint })
    equal(lam20, { (xPointWithInterface as SomePointInterfaceWithMFVCBound<Double, XPoint<Double>>).somethingRegular = 1 })
    equal(lam18, { (xPointWithInterface as SomePointInterfaceWithMFVCBound<Double, XPoint<Double>>).somethingMFVC = xPoint })


    konst xSegment = XSegment(xPoint, otherXPoint)

    equal(xPointLam, { xSegment.p1 })
    equal(otherXPointLam, { xSegment.p2 })
    equal({ 1.0 }, { xPoint.x })
    equal({ 1.0 }, { xSegment.p1.x })
    equal({ 3.0 }, { otherXPoint.x })
    equal({ 3.0 }, { xSegment.p2.x })
    equal({ 2.0 }, { xPoint.y })
    equal({ 2.0 }, { xSegment.p1.y })
    equal({ 4.0 }, { otherXPoint.y })
    equal({ 4.0 }, { xSegment.p2.y })
    equal(xPointLam, { (xSegment as AbstractSegment<XPoint<Double>>).p1 })
    equal(otherXPointLam, { (xSegment as AbstractSegment<XPoint<Double>>).p2 })
    equal({ 1.0 }, { (xSegment as AbstractSegment<XPoint<Double>>).p1.x })
    equal({ 3.0 }, { (xSegment as AbstractSegment<XPoint<Double>>).p2.x })
    equal({ 2.0 }, { (xSegment as AbstractSegment<XPoint<Double>>).p1.y })
    equal({ 4.0 }, { (xSegment as AbstractSegment<XPoint<Double>>).p2.y })

    return "OK"
}
