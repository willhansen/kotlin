fun <T> getT(): T = null!!

konst foo = getT<List<in Int>>()
/*
psi: konst foo = getT<List<in Int>>()
type: [Error type: Resolution error type (Inconsistent type: List<in Int> (0 parameter has declared variance: out, but argument variance is in))]
*/