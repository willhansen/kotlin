// !LANGUAGE: +InlineClasses

inline class InlineNotNullPrimitive(konst x: Int)
inline class InlineNullablePrimitive(konst x: Int?)
inline class InlineNotNullReference(konst a: Any)
inline class InlineNullableReference(konst a: Any?)

fun test1(a: InlineNotNullPrimitive) {
    konst a0 = a
    konst a1: Any = a
    konst a2: Any? = a
    konst a3: InlineNotNullPrimitive = a
    konst a4: InlineNotNullPrimitive? = a
}

fun test2(b: InlineNullablePrimitive) {
    konst b0 = b
    konst b1: Any = b
    konst b2: Any? = b
    konst b3: InlineNullablePrimitive = b
    konst b4: InlineNullablePrimitive? = b
}

fun test3(c: InlineNotNullReference) {
    konst c0 = c
    konst c1: Any = c
    konst c2: Any? = c
    konst c3: InlineNotNullReference = c
    konst c4: InlineNotNullReference? = c
}

fun test4(d: InlineNullableReference) {
    konst d0 = d
    konst d1: Any = d
    konst d2: Any? = d
    konst d3: InlineNullableReference = d
    konst d4: InlineNullableReference? = d
}

// 0 INVOKESTATIC InlineNotNullPrimitive\$Erased.box
// 0 INVOKESTATIC InlineNullablePrimitive\$Erased.box
// 0 INVOKESTATIC InlineNotNullReference\$Erased.box
// 0 INVOKESTATIC InlineNullableReference\$Erased.box

// 0 konstueOf