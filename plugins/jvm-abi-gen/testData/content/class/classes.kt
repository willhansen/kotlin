package test

interface Interface {
}

open class BaseClass {
    open konst baseClassPublicVal: Int = 0
    open fun baseClassPublicFun(): Int = 1

    internal konst baseClassInternalVal: Int = 2
    internal fun baseClassInternalFun(): Int = 3

    protected konst baseClassProtectedVal: Int = 4
    protected fun baseClassProtectedFun(): Int = 5

    private konst baseClassPrivateVal: Int = 6
    private fun baseClassPrivateFun(): Int = 7

    companion object {
        const konst basePublicConst: Int = 8
        private const konst basePrivateConst: Int = 9
    }
}

class Class : BaseClass(), Interface {
    fun classPublicMethod() {
        class publicMethodLocalClass {
            konst x = 0
        }

        konst publicMethodLambda: (Int) -> Int = { it * it }
    }

    private class NestedInnerClass() {
        class NestedNestedInnerClass() {}
    }
}

private class PrivateClass