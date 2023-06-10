// FIR_IDENTICAL
// !CHECK_TYPE
fun test() {
    konst a = if (true) {
        konst x = 1
        ({ x })
    } else {
        { 2 }
    }
    a checkType {  _<() -> Int>() }
}