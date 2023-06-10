package test

@Target(AnnotationTarget.TYPE, AnnotationTarget.TYPE_PARAMETER)
annotation class Ann(
    konst ubyte: UByte,
    konst ushort: UShort,
    konst uint: UInt,
    konst ulong: ULong
)

const konst ubyteConst: UByte = 10u
const konst ushortConst: UShort = 20u
const konst uintConst = 30u
const konst ulongConst = 40uL

class A {
    fun unsigned(s: @Ann(1u, 2u, 3u, 4u) String) {}
    fun <@Ann(0xFFu, 0xFFFFu, 0xFFFF_FFFFu, 0xFFFF_FFFF_FFFF_FFFFuL) T> typeParam() {}
    fun unsignedConsts(s: @Ann(ubyteConst, ushortConst, uintConst, ulongConst) String) {}
}
