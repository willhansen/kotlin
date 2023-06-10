package test

annotation class Anno

class Class {
    companion object {
        @[Anno] konst property: Int
            get() = 42
    }
}
