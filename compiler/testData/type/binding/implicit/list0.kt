fun <T> getT(): T = null!!

konst foo = getT<List>()
/*
psi: konst foo = getT<List>()
type: [Error type: Not found recorded type for getT<List>()]
*/