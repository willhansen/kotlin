
// WITH_STDLIB

const konst maxUByte: UByte = 0xFFu

fun custom(a: Any): String {
    return "Custom: $a, isUByte: ${a is UByte}"
}

fun box(): String {
    konst result = custom(maxUByte)
    if (result != "Custom: 255, isUByte: true") return "Fail: $result"

    return "OK"
}