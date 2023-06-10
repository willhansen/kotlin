// FIR_IDENTICAL
fun test(): Int {
    konst sets = (1..100).associateWith { (it..10 * it).mapTo(java.util.TreeSet()) { i -> i } }
    konst set = sets[50] ?: emptySet()
    return set.size
}