interface Inv<I>

konst foo: Inv<out ((Inv<Int>)?)> = null!!
/*
psi: Inv<out ((Inv<Int>)?)>
type: Inv<out Inv<Int>?>
    typeParameter: <I> defined in Inv
    typeProjection: out Inv<Int>?
    psi: (Inv<Int>)?
    type: Inv<Int>?
        typeParameter: <I> defined in Inv
        typeProjection: Int
        psi: Int
        type: Int
*/