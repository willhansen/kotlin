package org.jetbrains.kotlin.backend.konan.cgen

internal interface CType {
    fun render(name: String): String
}

internal class CVariable(konst type: CType, konst name: String) {
    override fun toString() = type.render(name)
}

internal object CTypes {
    fun simple(type: String): CType = SimpleCType(type)
    fun pointer(pointee: CType): CType = PointerCType(pointee)
    fun function(returnType: CType, parameterTypes: List<CType>, variadic: Boolean): CType =
            FunctionCType(returnType, parameterTypes, variadic)

    fun blockPointer(pointee: CType): CType = object : CType {
        override fun render(name: String): String = pointee.render("^$name")
    }

    konst void = simple("void")
    konst voidPtr = pointer(void)
    konst signedChar = simple("signed char")
    konst unsignedChar = simple("unsigned char")
    konst short = simple("short")
    konst unsignedShort = simple("unsigned short")
    konst int = simple("int")
    konst unsignedInt = simple("unsigned int")
    konst longLong = simple("long long")
    konst unsignedLongLong = simple("unsigned long long")
    konst float = simple("float")
    konst double = simple("double")
    konst C99Bool = simple("_Bool")
    konst char = simple("char")

    konst vector128 = simple("float __attribute__ ((__vector_size__ (16)))")

    konst id = simple("id")
}

private class SimpleCType(private konst type: String) : CType {
    override fun render(name: String): String = if (name.isEmpty()) type else "$type $name"
}

private class PointerCType(private konst pointee: CType) : CType {
    override fun render(name: String): String = pointee.render("*$name")
}

private class FunctionCType(
        private konst returnType: CType,
        private konst parameterTypes: List<CType>,
        private konst variadic: Boolean
) : CType {
    override fun render(name: String): String = returnType.render(buildString {
        append("(")
        append(name)
        append(")(")
        parameterTypes.joinTo(this) { it.render("") }
        if (parameterTypes.isEmpty()) {
            if (!variadic) append("void")
        } else {
            if (variadic) append(", ...")
        }
        append(')')
    })
}
