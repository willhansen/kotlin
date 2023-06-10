fun <T> getT(): T = null!!

konst foo = getT<Pair<List<Int>, String>>()
/*
psi: konst foo = getT<Pair<List<Int>, String>>()
type: Pair<List<Int>, String>
    typeParameter: <out A> defined in kotlin.Pair
    typeProjection: List<Int>
    psi: konst foo = getT<Pair<List<Int>, String>>()
    type: List<Int>
        typeParameter: <out E> defined in kotlin.collections.List
        typeProjection: Int
        psi: konst foo = getT<Pair<List<Int>, String>>()
        type: Int

    typeParameter: <out B> defined in kotlin.Pair
    typeProjection: String
    psi: konst foo = getT<Pair<List<Int>, String>>()
    type: String
*/