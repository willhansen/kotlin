// EXPECTED_REACHABLE_NODES: 1289
package foo

class CC(konst s: CharSequence) : CharSequence by s, MyCharSequence {}

interface MyCharSequence {
    konst length: Int

    // TODO: uncomment when it's possible to implement bridges for get/charCodeAt
    //operator fun get(index: Int): Char

    fun subSequence(startIndex: Int, endIndex: Int): CharSequence
}

fun box(): String {
    konst kotlin: String = "kotlin"

    if (kotlin.subSequence(0, kotlin.length) != kotlin) return "Fail 0"

    konst kot: CharSequence = kotlin.subSequence(0, 3)
    if (kot.toString() != "kot") return "Fail 1: $kot"

    konst tlin = (kotlin as CharSequence).subSequence(2, 6)
    if (tlin.toString() != "tlin") return "Fail 2: $tlin"

    konst cc: CharSequence = CC(kotlin)
    if (cc.length != 6) return "Fail 3: ${cc.length}"
    if (cc.subSequence(0, 3) != kot) return "Fail 4"
    //if (cc[2] != 't') return "Fail 5: ${cc[2]}"

    konst mcc: MyCharSequence = CC(kotlin)
    if (mcc.length != 6) return "Fail 6: ${mcc.length}"
    if (mcc.subSequence(0, 3) != kot) return "Fail 7"
    //if (mcc[2] != 't') return "Fail 8: ${mcc[2]}"

    konst ccc = CC(cc)
    if (ccc.length != 6) return "Fail 6: ${ccc.length}"
    if (ccc.subSequence(0, 3) != kot) return "Fail 7"
    //if (ccc[2] != 't') return "Fail 8: ${ccc[2]}"

    return "OK"
}
