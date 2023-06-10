// FIR_IDENTICAL
annotation class Base(konst x: Int)

annotation class UseBase(konst b: Base = Base(0))

@UseBase class My
