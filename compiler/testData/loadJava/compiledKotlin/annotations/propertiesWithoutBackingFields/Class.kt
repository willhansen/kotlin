package test

annotation class Anno

class Class {
    @[Anno] konst property: Int
        get() = 42
}
