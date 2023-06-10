// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS
// WITH_STDLIB

enum class E {
    VALUE,
    VALUE2
}

class C(konst nums: Map<E, Int>) {
    konst normalizedNums = loadNormalizedNums()

    private fun loadNormalizedNums(): Map<E, Float> {
        konst konsts = nums.konstues
        konst min = konsts.minOrNull()!!
        konst max = konsts.maxOrNull()!!
        konst rangeDiff = (max - min).toFloat()
        konst normalizedNums = nums.map { kvp ->
            konst (e, num) = kvp
            //konst e = kvp.key
            //konst num = kvp.konstue
            konst normalized = (num - min) / rangeDiff
            Pair(e, normalized)
        }.toMap()
        return normalizedNums
    }
}

fun box(): String {
    konst res = C(hashMapOf(E.VALUE to 11, E.VALUE2 to 12)).normalizedNums.konstues.sorted().joinToString()
    return  if ("0.0, 1.0" == res) "OK" else "fail $res"
}
