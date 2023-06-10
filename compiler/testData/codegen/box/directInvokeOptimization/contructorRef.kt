// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 0 invoke\(

class C(x: String, y: String) {
    konst yx = y + x
}

fun box() =
    ::C.invoke("K", "O").yx
