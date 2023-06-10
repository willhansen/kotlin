fun <T> getT(): T = null!!

konst foo = getT<Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>>()
/*
psi: konst foo = getT<Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>>()
type: Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>
    typeParameter: <out A> defined in kotlin.Pair
    typeProjection: List<Int?>
    psi: konst foo = getT<Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>>()
    type: List<Int?>
        typeParameter: <out E> defined in kotlin.collections.List
        typeProjection: Int?
        psi: konst foo = getT<Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>>()
        type: Int?

    typeParameter: <out B> defined in kotlin.Pair
    typeProjection: Pair<List<Int?>?, List<Int>?>
    psi: konst foo = getT<Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>>()
    type: Pair<List<Int?>?, List<Int>?>
        typeParameter: <out A> defined in kotlin.Pair
        typeProjection: List<Int?>?
        psi: konst foo = getT<Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>>()
        type: List<Int?>?
            typeParameter: <out E> defined in kotlin.collections.List
            typeProjection: Int?
            psi: konst foo = getT<Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>>()
            type: Int?

        typeParameter: <out B> defined in kotlin.Pair
        typeProjection: List<Int>?
        psi: konst foo = getT<Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>>()
        type: List<Int>?
            typeParameter: <out E> defined in kotlin.collections.List
            typeProjection: Int
            psi: konst foo = getT<Pair<List<Int?>, Pair<List<Int?>?, List<Int>?>>>()
            type: Int
*/