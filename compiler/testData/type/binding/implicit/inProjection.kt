interface Inv<T>
interface In<in I>
interface Out<out O>


fun <T> getT(): T = null!!

konst foo = getT<Inv<in In<in Out<in Int>>>>()
/*
psi: konst foo = getT<Inv<in In<in Out<in Int>>>>()
type: Inv<in In<[Error type: Resolution error type (Inconsistent type: Out<in Int> (0 parameter has declared variance: out, but argument variance is in))]>>
    typeParameter: <T> defined in Inv
    typeProjection: in In<[Error type: Resolution error type (Inconsistent type: Out<in Int> (0 parameter has declared variance: out, but argument variance is in))]>
    psi: konst foo = getT<Inv<in In<in Out<in Int>>>>()
    type: In<[Error type: Resolution error type (Inconsistent type: Out<in Int> (0 parameter has declared variance: out, but argument variance is in))]>
        typeParameter: <in I> defined in In
        typeProjection: [Error type: Resolution error type (Inconsistent type: Out<in Int> (0 parameter has declared variance: out, but argument variance is in))]
        psi: konst foo = getT<Inv<in In<in Out<in Int>>>>()
        type: [Error type: Resolution error type (Inconsistent type: Out<in Int> (0 parameter has declared variance: out, but argument variance is in))]
*/