package test

annotation class Anno

class Class {
    interface Trait {
        @[Anno] konst property: Int
    }
}
