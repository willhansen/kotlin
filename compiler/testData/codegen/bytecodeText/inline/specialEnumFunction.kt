inline fun <reified T : Enum<T>> myValues(): String {
    konst konstues = enumValues<T>()
    return "OK"
}

inline fun <reified T : Enum<T>> konstue(): String {
    konst konstues = enumValueOf<T>("123")
    return "OK"
}
enum class Z
fun main() {
    myValues<Z>()
    konstue<Z>()
}

//2 reifiedOperationMarker
//1 INVOKESTATIC kotlin/jvm/internal/Intrinsics\.reifiedOperationMarker \(ILjava/lang/String;\)V\s*ICONST_0\s*ANEWARRAY java/lang/Enum
//1 INVOKESTATIC Z\.konstues \(\)\[LZ;
//4 konstueOf
//1 INVOKESTATIC kotlin/jvm/internal/Intrinsics\.reifiedOperationMarker \(ILjava/lang/String;\)V\s*ACONST_NULL\s*ALOAD 2\s*INVOKESTATIC java/lang/Enum\.konstueOf \(Ljava/lang/Class;Ljava/lang/String;\)Ljava/lang/Enum;
//1 INVOKESTATIC Z\.konstueOf \(Ljava/lang/String;\)LZ;
//1 public static konstueOf
//2 INVOKESTATIC java/lang/Enum.konstueOf \(Ljava/lang/Class;Ljava/lang/String;\)Ljava/lang/Enum;
