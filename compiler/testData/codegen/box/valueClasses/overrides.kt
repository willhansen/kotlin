// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses
// CHECK_BYTECODE_LISTING

interface AbstractPoint<T> {
    konst x: T
    konst y: T
}

@JvmInline
konstue class DPoint(override konst x: Double, override konst y: Double): AbstractPoint<Double>

interface GenericMFVCHolder<T> {
    var p: T
    var p1: T
}

interface GenericMFVCHolderWithMFVCUpperBound<T : DPoint> {
    var p: T
    var p1: T
}

interface ReifiedMFVCHolder {
    var p: DPoint
    var p1: DPoint
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

interface GenericMFVCHolderWithMFVCUpperBoundWithImpls<T : DPoint> {
    var p: T
        get() = throw DataClassException(5)
        set(konstue) = throw DataClassException(6 to konstue)

    var p1: T
        get() = throw DataClassException(7)
        set(konstue) = throw DataClassException(8 to konstue)
}

interface ReifiedMFVCHolderWithImpls {
    var p: DPoint
        get() = throw DataClassException(9)
        set(konstue) = throw DataClassException(10 to konstue)

    var p1: DPoint
        get() = throw DataClassException(11)
        set(konstue) = throw DataClassException(12 to konstue)
}

class RealOverride(override var p: DPoint) : GenericMFVCHolder<DPoint>, ReifiedMFVCHolder, GenericMFVCHolderWithMFVCUpperBound<DPoint> {
    override var p1: DPoint
        get() = throw DataClassException(13)
        set(konstue) = throw DataClassException(14 to konstue)
}

class GenericFakeOverride : GenericMFVCHolderWithImpls<DPoint>
class ReifiedFakeOverride : ReifiedMFVCHolderWithImpls
class GenericFakeOverrideWithMFVCUpperBound : GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>


@JvmInline
konstue class GenericFakeOverrideMFVC(konst field1: Double, konst field2: Double) : GenericMFVCHolderWithImpls<DPoint>
@JvmInline
konstue class ReifiedFakeOverrideMFVC(konst field1: Double, konst field2: Double) : ReifiedMFVCHolderWithImpls
@JvmInline
konstue class GenericFakeOverrideMFVCWithMFVCUpperBound(konst field1: Double, konst field2: Double) : GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>


interface SomePointInterface<T> {
    var somethingRegular: Int

    var somethingGeneric: T

    var somethingMFVC: DPoint
}

interface SomePointInterfaceWithMFVCBound<T : DPoint> {
    var somethingRegular: Int

    var somethingGeneric: T

    var somethingMFVC: DPoint
}

@JvmInline
konstue class DPointWithInterface(konst x: Double, konst y: Double) : SomePointInterface<DPoint>, SomePointInterfaceWithMFVCBound<DPoint> {
    override var somethingGeneric: DPoint
        get() = throw DataClassException(15)
        set(konstue) = throw DataClassException(16 to konstue)

    override var somethingMFVC: DPoint
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
konstue class DSegment(override konst p1: DPoint, override konst p2: DPoint): AbstractSegment<DPoint>

fun <T> equal(expected: () -> T, actual: () -> T) {
    konst expectedResult = runCatching { expected() }
    konst actualResult = runCatching { actual() }
    require(expectedResult == actualResult) { "Expected: $expectedResult\nActual: $actualResult" }
}

fun box(): String {
    konst dPoint = DPoint(1.0, 2.0)
    
    konst lam1: () -> DPoint = { throw DataClassException(1) }
    konst lam2: () -> Unit = { throw DataClassException(2 to dPoint) }
    konst lam3: () -> DPoint = { throw DataClassException(3) }
    konst lam4: () -> Unit = { throw DataClassException(4 to dPoint) }
    konst lam5: () -> DPoint = { throw DataClassException(5) }
    konst lam6: () -> Unit = { throw DataClassException(6 to dPoint) }
    konst lam7: () -> DPoint = { throw DataClassException(7) }
    konst lam8: () -> Unit = { throw DataClassException(8 to dPoint) }
    konst lam9: () -> DPoint = { throw DataClassException(9) }
    konst lam10: () -> Unit = { throw DataClassException(10 to dPoint) }
    konst lam11: () -> DPoint = { throw DataClassException(11) }
    konst lam12: () -> Unit = { throw DataClassException(12 to dPoint) }
    konst lam13: () -> DPoint = { throw DataClassException(13) }
    konst lam14: () -> Unit = { throw DataClassException(14 to dPoint) }
    konst lam15: () -> DPoint = { throw DataClassException(15) }
    konst lam16: () -> Unit = { throw DataClassException(16 to dPoint) }
    konst lam17: () -> DPoint = { throw DataClassException(17) }
    konst lam18: () -> Unit = { throw DataClassException(18 to dPoint) }
    konst lam19: () -> Int = { throw DataClassException(19) }
    konst lam20: () -> Unit = { throw DataClassException(20 to 1) }
    konst emptyLam = {}
    konst dPointLam = { dPoint }
    konst otherDPoint = DPoint(3.0, 4.0)
    konst otherDPointLam = { otherDPoint }
    equal({ "DPoint(x=1.0, y=2.0)" }, { dPoint.toString() })
    equal({ "DPoint(x=1.0, y=2.0)" }, { (dPoint as Any).toString() })

    equal({ true }, { dPoint.equals(dPoint) })
    equal({ true }, { dPoint.equals(dPoint as Any) })
    equal({ true }, { (dPoint as Any).equals(dPoint) })
    equal({ true }, { (dPoint as Any).equals(dPoint as Any) })

    equal({ false }, { dPoint.equals(otherDPoint) })
    equal({ false }, { dPoint.equals(otherDPoint as Any) })
    equal({ false }, { (dPoint as Any).equals(otherDPoint) })
    equal({ false }, { (dPoint as Any).equals(otherDPoint as Any) })

    equal({ dPoint.hashCode() }, { (dPoint as Any).hashCode() })

    equal({ 1.0 }, { dPoint.x })
    equal({ 1.0 }, { (dPoint as AbstractPoint<Double>).x })
    equal({ 2.0 }, { dPoint.y })
    equal({ 2.0 }, { (dPoint as AbstractPoint<Double>).y })


    konst realOverride = RealOverride(dPoint)

    equal(dPointLam, { realOverride.p })
    equal(dPointLam, { (realOverride as GenericMFVCHolder<DPoint>).p })
    equal(lam13, { realOverride.p1 })
    equal(lam13, { (realOverride as GenericMFVCHolder<DPoint>).p1 })
    equal(dPointLam, { (realOverride as ReifiedMFVCHolder).p })
    equal(lam13, { realOverride.p1 })
    equal(lam13, { (realOverride as ReifiedMFVCHolder).p1 })
    equal(dPointLam, { (realOverride as GenericMFVCHolderWithMFVCUpperBound<DPoint>).p })
    equal(lam13, { (realOverride as GenericMFVCHolderWithMFVCUpperBound<DPoint>).p1 })

    
    equal(emptyLam, { realOverride.p = dPoint })
    equal(emptyLam, { (realOverride as GenericMFVCHolder<DPoint>).p = dPoint })
    equal(lam14, { realOverride.p1 = dPoint })
    equal(lam14, { (realOverride as GenericMFVCHolder<DPoint>).p1 = dPoint })
    equal(emptyLam, { (realOverride as ReifiedMFVCHolder).p = dPoint })
    equal(lam14, { (realOverride as ReifiedMFVCHolder).p1 = dPoint })
    equal(emptyLam, { (realOverride as GenericMFVCHolderWithMFVCUpperBound<DPoint>).p = dPoint })
    equal(lam14, { (realOverride as GenericMFVCHolderWithMFVCUpperBound<DPoint>).p1 = dPoint })


    konst genericFakeOverride = GenericFakeOverride()

    equal(lam1, { genericFakeOverride.p })
    equal(lam1, { (genericFakeOverride as GenericMFVCHolderWithImpls<DPoint>).p })
    equal(lam3, { genericFakeOverride.p1 })
    equal(lam3, { (genericFakeOverride as GenericMFVCHolderWithImpls<DPoint>).p1 })
    konst reifiedFakeOverride = ReifiedFakeOverride()
    equal(lam9, { reifiedFakeOverride.p })
    equal(lam9, { (reifiedFakeOverride as ReifiedMFVCHolderWithImpls).p })
    equal(lam11, { reifiedFakeOverride.p1 })
    equal(lam11, { (reifiedFakeOverride as ReifiedMFVCHolderWithImpls).p1 })
    konst genericFakeOverrideWithMFVCUpperBound = GenericFakeOverrideWithMFVCUpperBound()
    equal(lam5, { genericFakeOverrideWithMFVCUpperBound.p })
    equal(lam5, { (genericFakeOverrideWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>).p })
    equal(lam7, { genericFakeOverrideWithMFVCUpperBound.p1 })
    equal(lam7, { (genericFakeOverrideWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>).p1 })
    
    equal(lam2, { genericFakeOverride.p = dPoint })
    equal(lam2, { (genericFakeOverride as GenericMFVCHolderWithImpls<DPoint>).p = dPoint })
    equal(lam4, { genericFakeOverride.p1 = dPoint })
    equal(lam4, { (genericFakeOverride as GenericMFVCHolderWithImpls<DPoint>).p1 = dPoint })
    equal(lam10, { reifiedFakeOverride.p = dPoint })
    equal(lam10, { (reifiedFakeOverride as ReifiedMFVCHolderWithImpls).p = dPoint })
    equal(lam12, { reifiedFakeOverride.p1 = dPoint })
    equal(lam12, { (reifiedFakeOverride as ReifiedMFVCHolderWithImpls).p1 = dPoint })
    equal(lam6, { genericFakeOverrideWithMFVCUpperBound.p = dPoint })
    equal(lam6, { (genericFakeOverrideWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>).p = dPoint })
    equal(lam8, { genericFakeOverrideWithMFVCUpperBound.p1 = dPoint })
    equal(lam8, { (genericFakeOverrideWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>).p1 = dPoint })
    
    
    konst genericFakeOverrideMFVC = GenericFakeOverrideMFVC(1.0, 2.0)

    equal(lam1, { genericFakeOverrideMFVC.p })
    equal(lam1, { (genericFakeOverrideMFVC as GenericMFVCHolderWithImpls<DPoint>).p })
    equal(lam3, { genericFakeOverrideMFVC.p1 })
    equal(lam3, { (genericFakeOverrideMFVC as GenericMFVCHolderWithImpls<DPoint>).p1 })

    konst reifiedFakeOverrideMFVC = ReifiedFakeOverrideMFVC(1.0, 2.0)
    equal(lam9, { reifiedFakeOverrideMFVC.p })
    equal(lam9, { (reifiedFakeOverrideMFVC as ReifiedMFVCHolderWithImpls).p })
    equal(lam11, { reifiedFakeOverrideMFVC.p1 })
    equal(lam11, { (reifiedFakeOverrideMFVC as ReifiedMFVCHolderWithImpls).p1 })

    konst genericFakeOverrideMFVCWithMFVCUpperBound = GenericFakeOverrideMFVCWithMFVCUpperBound(1.0, 2.0)
    equal(lam5, { genericFakeOverrideMFVCWithMFVCUpperBound.p })
    equal(lam5, { (genericFakeOverrideMFVCWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>).p })
    equal(lam7, { genericFakeOverrideMFVCWithMFVCUpperBound.p1 })
    equal(lam7, { (genericFakeOverrideMFVCWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>).p1 })

    equal(lam2, { genericFakeOverrideMFVC.p = dPoint })
    equal(lam2, { (genericFakeOverrideMFVC as GenericMFVCHolderWithImpls<DPoint>).p = dPoint })
    equal(lam4, { genericFakeOverrideMFVC.p1 = dPoint })
    equal(lam4, { (genericFakeOverrideMFVC as GenericMFVCHolderWithImpls<DPoint>).p1 = dPoint })

    equal(lam10, { reifiedFakeOverrideMFVC.p = dPoint })
    equal(lam10, { (reifiedFakeOverrideMFVC as ReifiedMFVCHolderWithImpls).p = dPoint })
    equal(lam12, { reifiedFakeOverrideMFVC.p1 = dPoint })
    equal(lam12, { (reifiedFakeOverrideMFVC as ReifiedMFVCHolderWithImpls).p1 = dPoint })

    equal(lam6, { genericFakeOverrideMFVCWithMFVCUpperBound.p = dPoint })
    equal(lam6, { (genericFakeOverrideMFVCWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>).p = dPoint })
    equal(lam8, { genericFakeOverrideMFVCWithMFVCUpperBound.p1 = dPoint })
    equal(lam8, { (genericFakeOverrideMFVCWithMFVCUpperBound as GenericMFVCHolderWithMFVCUpperBoundWithImpls<DPoint>).p1 = dPoint })


    konst dPointWithInterface = DPointWithInterface(1.0, 2.0)

    equal(lam15, { dPointWithInterface.somethingGeneric })
    equal(lam15, { (dPointWithInterface as SomePointInterface<DPoint>).somethingGeneric })
    equal(lam19, { dPointWithInterface.somethingRegular })
    equal(lam19, { (dPointWithInterface as SomePointInterface<DPoint>).somethingRegular })
    equal(lam17, { dPointWithInterface.somethingMFVC })
    equal(lam17, { (dPointWithInterface as SomePointInterface<DPoint>).somethingMFVC })

    equal(lam15, { (dPointWithInterface as SomePointInterfaceWithMFVCBound<DPoint>).somethingGeneric })
    equal(lam19, { (dPointWithInterface as SomePointInterfaceWithMFVCBound<DPoint>).somethingRegular })
    equal(lam17, { (dPointWithInterface as SomePointInterfaceWithMFVCBound<DPoint>).somethingMFVC })

    equal(lam16, { dPointWithInterface.somethingGeneric = dPoint })
    equal(lam16, { (dPointWithInterface as SomePointInterface<DPoint>).somethingGeneric = dPoint })
    equal(lam20, { dPointWithInterface.somethingRegular = 1 })
    equal(lam20, { (dPointWithInterface as SomePointInterface<DPoint>).somethingRegular = 1 })
    equal(lam18, { dPointWithInterface.somethingMFVC = dPoint })
    equal(lam18, { (dPointWithInterface as SomePointInterface<DPoint>).somethingMFVC = dPoint })

    equal(lam16, { (dPointWithInterface as SomePointInterfaceWithMFVCBound<DPoint>).somethingGeneric = dPoint })
    equal(lam20, { (dPointWithInterface as SomePointInterfaceWithMFVCBound<DPoint>).somethingRegular = 1 })
    equal(lam18, { (dPointWithInterface as SomePointInterfaceWithMFVCBound<DPoint>).somethingMFVC = dPoint })


    konst dSegment = DSegment(dPoint, otherDPoint)

    equal(dPointLam, { dSegment.p1 })
    equal(otherDPointLam, { dSegment.p2 })
    equal({ 1.0 }, { dPoint.x })
    equal({ 1.0 }, { dSegment.p1.x })
    equal({ 3.0 }, { otherDPoint.x })
    equal({ 3.0 }, { dSegment.p2.x })
    equal({ 2.0 }, { dPoint.y })
    equal({ 2.0 }, { dSegment.p1.y })
    equal({ 4.0 }, { otherDPoint.y })
    equal({ 4.0 }, { dSegment.p2.y })
    equal(dPointLam, { (dSegment as AbstractSegment<DPoint>).p1 })
    equal(otherDPointLam, { (dSegment as AbstractSegment<DPoint>).p2 })
    equal({ 1.0 }, { (dSegment as AbstractSegment<DPoint>).p1.x })
    equal({ 3.0 }, { (dSegment as AbstractSegment<DPoint>).p2.x })
    equal({ 2.0 }, { (dSegment as AbstractSegment<DPoint>).p1.y })
    equal({ 4.0 }, { (dSegment as AbstractSegment<DPoint>).p2.y })

    return "OK"
}
