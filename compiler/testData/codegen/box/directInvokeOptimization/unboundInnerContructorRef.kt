// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 0 invoke\(

class Outer (konst x: String) {
    inner class Inner(konst y: String) {
        konst yx = y + x
    }
}

fun box() =
    (Outer::Inner).invoke(Outer("K"), "O").yx
