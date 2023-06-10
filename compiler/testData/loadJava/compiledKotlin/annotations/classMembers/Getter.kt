// PLATFORM_DEPENDANT_METADATA
package test

annotation class Anno

class Class {
    konst property: Int
        @[Anno] get() = 42
}
