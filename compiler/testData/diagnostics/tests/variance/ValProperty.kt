// FIR_IDENTICAL
import kotlin.reflect.KProperty

interface In<in T>
interface Out<out T>
interface Inv<T>

class Delegate<T> {
    operator fun getValue(t: Any, p: KProperty<*>): T = null!!
    operator fun setValue(t: Any, p: KProperty<*>, konstue: T) {}
}

fun <T> getT(): T = null!!

abstract class Test<in I, out O, P> {
    abstract konst type1: <!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>
    abstract konst type2: O
    abstract konst type3: P
    abstract konst type4: In<I>
    abstract konst type5: In<<!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>>

    <!TYPE_VARIANCE_CONFLICT_ERROR!>konst implicitType1<!> = getT<I>()
    konst implicitType2 = getT<O>()
    konst implicitType3 = getT<P>()
    konst implicitType4 = getT<In<I>>()
    <!TYPE_VARIANCE_CONFLICT_ERROR!>konst implicitType5<!> = getT<In<O>>()

    <!TYPE_VARIANCE_CONFLICT_ERROR!>konst delegateType1<!> by Delegate<I>()
    konst delegateType2 by Delegate<O>()
    konst delegateType3 by Delegate<P>()
    konst delegateType4 by Delegate<In<I>>()
    <!TYPE_VARIANCE_CONFLICT_ERROR!>konst delegateType5<!> by Delegate<In<O>>()

    abstract konst I.receiver1: Int
    abstract konst <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>.receiver2: Int
    abstract konst P.receiver3: Int
    abstract konst In<<!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>>.receiver4: Int
    abstract konst In<O>.receiver5: Int

    konst <X : I> X.typeParameter1: Int get() = 0
    konst <X : <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>> X.typeParameter2: Int get() = 0
    konst <X : P> X.typeParameter3: Int get() = 0
    konst <X : In<<!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>>> X.typeParameter4: Int get() = 0
    konst <X : In<O>> X.typeParameter5: Int get() = 0

    konst <X> X.typeParameter6: Int where X : I get() = 0
    konst <X> X.typeParameter7: Int where X : <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!> get() = 0
    konst <X> X.typeParameter8: Int where X : P get() = 0
    konst <X> X.typeParameter9: Int where X : In<<!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>> get() = 0
    konst <X> X.typeParameter0: Int where X : In<O> get() = 0
}
