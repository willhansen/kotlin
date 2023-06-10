package test

annotation class Primitives(
        konst byte: Byte,
        konst char: Char,
        konst short: Short,
        konst int: Int,
        konst long: Long,
        konst float: Float,
        konst double: Double,
        konst boolean: Boolean
)

@Primitives(
        byte = 7,
        char = '%',
        short = 239,
        int = 239017,
        long = 123456789123456789L,
        float = 2.72f,
        double = -3.14,
        boolean = true
)
class C
