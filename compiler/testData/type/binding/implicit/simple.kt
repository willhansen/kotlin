fun <T> getT(): T = null!!

konst foo = getT<List<Int>>()
/*
psi: konst foo = getT<List<Int>>()
type: List<Int>
    typeParameter: <out E> defined in kotlin.collections.List
    typeProjection: Int
    psi: konst foo = getT<List<Int>>()
    type: Int
*/