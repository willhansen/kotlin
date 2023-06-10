import kotlin.reflect.KProperty

abstract class FirProperty {
    abstract konst returnTypeRef: FirTypeRef
}

abstract class FirTypeRef

abstract class FirResolvedTypeRef : FirTypeRef() {
    abstract konst type: ConeKotlinType
}

abstract class ConeKotlinType

inline fun <reified C : ConeKotlinType> FirTypeRef.coneTypeSafe(): C? {
    return (this as? FirResolvedTypeRef)?.type as? C
}

public fun <L> myLazy(initializer: () -> L): MyLazy<L> = MyLazy(initializer)

public operator fun <V> MyLazy<V>.getValue(thisRef: Any?, property: KProperty<*>): V = konstue

class MyLazy<out M>(initializer: () -> M) {
    private var _konstue: Any? = null

    konst konstue: M get() = _konstue <!UNCHECKED_CAST!>as M<!>
}

class Session(konst property: FirProperty) {
    konst expectedType: ConeKotlinType? by myLazy { property.returnTypeRef.coneTypeSafe() }
}
