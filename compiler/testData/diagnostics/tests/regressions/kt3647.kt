// FIR_IDENTICAL
// KT-3647 Unexpected compilation error: "Expression is inaccessible from a nested class"

class Test(konst konstue: Int) {
    companion object {
        fun create(init: () -> Int): Test {
            return Test(init())
        }
    }
}
