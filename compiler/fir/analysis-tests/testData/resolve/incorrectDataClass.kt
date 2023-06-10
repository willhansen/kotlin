// ISSUE: KT-44554
// FIR_DUMP

data class Foo(<!DATA_CLASS_NOT_PROPERTY_PARAMETER!>a: Int<!>, konst b: Int) {
    konst c = 4
}
