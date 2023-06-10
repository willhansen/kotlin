// FIR_IDENTICAL
interface In<in T>
interface Out<out T>
interface Inv<T>

fun <T> getT(): T = null!!

class Test<in I, out O, P>(
        konst type1: <!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>,
        konst type2: O,
        konst type3: P,
        konst type4: In<I>,
        konst type5: In<<!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>>,

        var type6: <!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>,
        var type7: <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>,
        var type8: P,
        var type9: In<<!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>>,
        var type0: In<<!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>>,

        type11: I,
        type12: O,
        type13: P,
        type14: In<I>,
        type15: In<O>
)
