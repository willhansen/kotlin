const konst aConst = 1
const konst bConst = aConst + 1

const konst boolVal = bConst > 1 || (B.boolVal && A.boolVal)
const konst stringInterpolation = "Result: ${B.boolVal}"

object A {
    const konst boolVal = bConst + 3 == 5

    const konst recursive1: Int = 1 + B.recursive2
}

class B {
    companion object {
        const konst boolVal = A.boolVal
        const konst recursive2: Int = A.recursive1 + 2
    }
}
