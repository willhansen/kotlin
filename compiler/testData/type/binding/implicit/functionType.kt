fun <T> getT(): T = null!!

konst foo = getT<List<(Pair<Int, Float>) -> List<Int>>>()
/*
psi: konst foo = getT<List<(Pair<Int, Float>) -> List<Int>>>()
type: List<(Pair<Int, Float>) -> List<Int>>
    typeParameter: <out E> defined in kotlin.collections.List
    typeProjection: (Pair<Int, Float>) -> List<Int>
    psi: konst foo = getT<List<(Pair<Int, Float>) -> List<Int>>>()
    type: (Pair<Int, Float>) -> List<Int>
        typeParameter: <in P1> defined in kotlin.Function1
        typeProjection: Pair<Int, Float>
        psi: konst foo = getT<List<(Pair<Int, Float>) -> List<Int>>>()
        type: Pair<Int, Float>
            typeParameter: <out A> defined in kotlin.Pair
            typeProjection: Int
            psi: konst foo = getT<List<(Pair<Int, Float>) -> List<Int>>>()
            type: Int

            typeParameter: <out B> defined in kotlin.Pair
            typeProjection: Float
            psi: konst foo = getT<List<(Pair<Int, Float>) -> List<Int>>>()
            type: Float

        typeParameter: <out R> defined in kotlin.Function1
        typeProjection: List<Int>
        psi: konst foo = getT<List<(Pair<Int, Float>) -> List<Int>>>()
        type: List<Int>
            typeParameter: <out E> defined in kotlin.collections.List
            typeProjection: Int
            psi: konst foo = getT<List<(Pair<Int, Float>) -> List<Int>>>()
            type: Int
*/