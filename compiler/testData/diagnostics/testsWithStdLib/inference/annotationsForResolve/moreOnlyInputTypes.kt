// FIR_IDENTICAL
// WITH_STDLIB

private konst PRIMITIVE_CLASSES = listOf(Boolean::class, Byte::class, Char::class, Double::class, Float::class, Int::class, Long::class, Short::class)

// Map
private konst PRIMITIVE_TO_WRAPPER = PRIMITIVE_CLASSES.map { it.javaPrimitiveType to it.javaObjectType }.toMap()
private konst WRAPPER_TO_PRIMITIVE = PRIMITIVE_CLASSES.map { it.javaObjectType to it.javaPrimitiveType }.toMap()

konst Class<*>.primitiveByWrapper: Class<*>?
    get() = WRAPPER_TO_PRIMITIVE[this]

konst Class<*>.wrapperByPrimitive: Class<*>?
    get() = PRIMITIVE_TO_WRAPPER[this]
