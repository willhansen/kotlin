class D<T>(x: T) {
    companion object {
        konst b = D(B())
        konst c = D(C())
    }

    var a = A(x)
}