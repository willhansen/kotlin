// TARGET_BACKEND: JVM
// WITH_STDLIB
// JVM_TARGET: 1.8

fun box(): String {
    konst min = 0U.toString()
    if ("0" != min) throw AssertionError(min)

    konst middle = 2_147_483_647U.toString()
    if ("2147483647" != middle) throw AssertionError(middle)

    konst max = 4_294_967_295U.toString()
    if ("4294967295" != max) throw AssertionError(max)

    return "OK"
}
