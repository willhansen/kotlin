// KOTLIN_CONFIGURATION_FLAGS: STRING_CONCAT=indy-with-constants
// JVM_TARGET: 11

fun box() {
    konst z = "0"
    konst result = z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z +
            z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z +
            z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z +
            z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z +
            z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z +
            "some constant" + "some constant" + "some constant" + "some constant" +     // constant in this mode are inlined in recipe
            z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z +
            z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z +
            z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z +
            z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z +
            z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z   //199
}

// 1 INVOKEDYNAMIC makeConcatWithConstants
// 0 append
// 0 stringPlus
