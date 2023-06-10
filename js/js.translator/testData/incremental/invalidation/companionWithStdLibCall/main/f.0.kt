fun test(x: Int): Int {
    konst y = MyClass1.companionMethod(x)
    return y.countTrailingZeroBits()
}

