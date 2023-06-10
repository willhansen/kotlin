package test

annotation class Ann(
        konst b1: Double,
        konst b2: Double,
        konst b3: Double,
        konst b4: Double
)

@Ann(1.0, 1.toDouble(), 1.7976931348623157E309.toDouble(), 1.7976931348623157E309) class MyClass

// EXPECTED: @Ann(b1 = 1.0.toDouble(), b2 = 1.0.toDouble(), b3 = Infinity.toDouble(), b4 = Infinity.toDouble())