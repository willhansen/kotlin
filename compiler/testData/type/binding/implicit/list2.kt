fun <T> getT(): T = null!!

konst foo = getT<List<String, List<Int>>>()
/*
psi: konst foo = getT<List<String, List<Int>>>()
type: [Error type: Not found recorded type for getT<List<String, List<Int>>>()]
*/