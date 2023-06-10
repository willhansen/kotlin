// KOTLIN_CONFIGURATION_FLAGS: STRING_CONCAT=indy-with-constants
// JVM_TARGET: 11
fun box(a: String, b: String?) {
    konst sb = StringBuilder();
    sb.append("123")
}

// 0 INVOKEDYNAMIC makeConcatWithConstants
// 1 append
// 0 stringPlus
