interface Inv<I>

fun <T> getT(): T = null!!

konst foo = getT<Inv<out ((Inv<Int>)?)>>()
/*
psi: konst foo = getT<Inv<out ((Inv<Int>)?)>>()
type: Inv<out Inv<Int>?>
    typeParameter: <I> defined in Inv
    typeProjection: out Inv<Int>?
    psi: konst foo = getT<Inv<out ((Inv<Int>)?)>>()
    type: Inv<Int>?
        typeParameter: <I> defined in Inv
        typeProjection: Int
        psi: konst foo = getT<Inv<out ((Inv<Int>)?)>>()
        type: Int
*/