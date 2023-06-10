// FIR_IDENTICAL
// From KT-13324: always succeeds
konst x = null as String?
// From KT-260: sometimes succeeds
fun foo(a: String?): Int? {
    konst c = a as? Int?
    return c
}