// !LANGUAGE: -ProperIeee754Comparisons
fun box(): String {
    konst zero: Any = 0.0
    konst floatZero: Any = -0.0F
    if (zero is Double && floatZero is Float) {
        if (zero == floatZero) return "fail 1"

        if (zero <= floatZero) return "fail 2"

        return "OK"
    }

    return "fail"
}

// 1 Intrinsics\.areEqual
// 1 Double\.compare
