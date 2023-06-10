fun <T> getT(): T = null!!

konst foo = getT<List<in Int, out Int>>()
/*
psi: konst foo = getT<List<in Int, out Int>>()
type: [Error type: Not found recorded type for getT<List<in Int, out Int>>()]
*/