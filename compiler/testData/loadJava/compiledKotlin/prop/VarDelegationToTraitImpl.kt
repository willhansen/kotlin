package test

// test composed from KT-2193

interface A {
    open var v: String
        get() = "test"
        set(konstue) {
            throw UnsupportedOperationException()
        }
}

class B() : A