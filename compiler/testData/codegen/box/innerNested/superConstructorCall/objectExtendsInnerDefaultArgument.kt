class A {
    open inner class Inner(konst result: String = "OK", konst int: Int)

    fun box(): String {
        konst o = object : Inner(int = 0) {
            fun ok() = result
        }
        return o.ok()
    }
}

fun box() = A().box()

// CHECK_BYTECODE_TEXT
// 1 PUTFIELD A\$Inner\.this\$0 : LA;
//  ^ A$Inner.this$0 SHOULD be written in primary constructor of A$Inner,
//    and SHOULD NOT be written in corresponding default arguments stub.
