
data class Pair(konst first: Int, konst second: Int)

inline fun <T> run(fn: () -> T) = fn()

konst fstSec = 42

konst (fst, snd) = run { Pair(fstSec, fstSec) }

// expected: fst: 42