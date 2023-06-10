fun <T> getT(): T = null!!

konst foo = getT<Pair<Pair<List<Int>>, String>>()
/*
psi: konst foo = getT<Pair<Pair<List<Int>>, String>>()
type: Pair<[Error type: Type for error type constructor (Pair)]<List<Int>>, String>
    typeParameter: <out A> defined in kotlin.Pair
    typeProjection: [Error type: Type for error type constructor (Pair)]<List<Int>>
    psi: konst foo = getT<Pair<Pair<List<Int>>, String>>()
    type: [Error type: Type for error type constructor (Pair)]<List<Int>>
        typeParameter: null
        typeProjection: List<Int>
        psi: konst foo = getT<Pair<Pair<List<Int>>, String>>()
        type: List<Int>
            typeParameter: <out E> defined in kotlin.collections.List
            typeProjection: Int
            psi: konst foo = getT<Pair<Pair<List<Int>>, String>>()
            type: Int

    typeParameter: <out B> defined in kotlin.Pair
    typeProjection: String
    psi: konst foo = getT<Pair<Pair<List<Int>>, String>>()
    type: String
*/