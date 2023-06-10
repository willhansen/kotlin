package test

annotation class IntAnno
annotation class StringAnno
annotation class DoubleAnno

@[IntAnno] konst Int.extension: Int
    get() = this

@[StringAnno] konst String.extension: String
    get() = this

@[DoubleAnno] konst Double.extension: Int
    get() = 42
