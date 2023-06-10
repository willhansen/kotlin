fun <T> getT(): T = null!!

konst foo = getT<List<adad<List<dd>>>()
/*
psi: konst foo = getT<List<adad<List<dd>>>()
type: [Error type: Not found recorded type for getT<List<adad<List<dd>>>()]
*/