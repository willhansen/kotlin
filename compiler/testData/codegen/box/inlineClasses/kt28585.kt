// WITH_STDLIB

fun <T> ekonst(fn: () -> T) = fn()

fun box(): String {
    var uint1 = 1u
    var uint2 = 2u
    var uint3 = 3u
    konst uintSet = mutableSetOf(uint1)
    uintSet.add(uint2);
    ekonst {
        uintSet.add(uint3)
        if (!uintSet.contains(1u)) throw AssertionError()
        if (!uintSet.contains(2u)) throw AssertionError()
        if (!uintSet.contains(3u)) throw AssertionError()
    }
    return "OK"
}