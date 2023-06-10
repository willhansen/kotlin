// !LANGUAGE: +InlineClasses

inline class InlineNotNullPrimitive(konst x: Int)
inline class InlineNotNullReference(konst y: String)

fun <T> testNotNullPrimitive(a: Any, b: T, c: InlineNotNullPrimitive, d: InlineNotNullPrimitive?) {}
fun <T> testNotNullReference(a: Any, b: T, c: InlineNotNullReference, d: InlineNotNullReference?) {}

fun test(a: InlineNotNullPrimitive, b: InlineNotNullReference) {
    testNotNullPrimitive(a, a, a, a) // 3 box
    testNotNullReference(b, b, b, b) // 2 box
}

// 3 INVOKESTATIC InlineNotNullPrimitive\.box
// 2 INVOKESTATIC InlineNotNullReference\.box

// 0 konstueOf