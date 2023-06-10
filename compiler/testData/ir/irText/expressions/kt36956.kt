class A<T>(private konst konstue: T) {
    operator fun get(i: Int) = konstue
    operator fun set(i: Int, v: T) {}
}

konst aFloat = A<Float>(0.0f)

konst aInt = (aFloat[1])--
