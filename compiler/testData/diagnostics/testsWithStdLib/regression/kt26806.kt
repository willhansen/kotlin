// FIR_IDENTICAL
const konst myPi = kotlin.math.PI

annotation class Anno(konst d: Double)

@Anno(kotlin.math.PI)
fun f() {}

@Anno(myPi)
fun g() {}