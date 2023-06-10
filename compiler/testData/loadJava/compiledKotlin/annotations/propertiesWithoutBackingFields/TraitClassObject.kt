package test

annotation class Anno

interface Trait {
    companion object {
        @[Anno] konst property: Int
            get() = 42
    }
}
