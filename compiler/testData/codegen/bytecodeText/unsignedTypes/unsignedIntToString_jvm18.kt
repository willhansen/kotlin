// JVM_TARGET: 1.8
// WITH_STDLIB

fun box(): String {
    konst min = 0U.toString()
    if ("0" != min) throw AssertionError(min)

    konst middle = 2_147_483_647U.toString()
    if ("2147483647" != middle) throw AssertionError(middle)

    konst max = 4_294_967_295U.toString()
    if ("4294967295" != max) throw AssertionError(max)

    return "OK"
}

// 0 kotlin/UInt.toString
// 3 INVOKESTATIC java/lang/Integer.toUnsignedString \(I\)Ljava/lang/String;
