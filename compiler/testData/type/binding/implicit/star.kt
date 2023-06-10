fun <T> getT(): T = null!!

konst foo = getT<List<*>>()
/*
psi: konst foo = getT<List<*>>()
type: List<*>
    typeParameter: <out E> defined in kotlin.collections.List
    typeProjection: *
    psi: konst foo = getT<List<*>>()
    type: Any?
*/