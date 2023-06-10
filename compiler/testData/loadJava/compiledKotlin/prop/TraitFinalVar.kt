// KT-2228

package test

interface A {
    var v: String
        get() = "test"
        set(konstue) {
            throw UnsupportedOperationException()
        }
}