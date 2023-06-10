package test

class outerClass<T>(konst t: T) {
    inner class innerClass {
        fun getT() = t
    }
}

fun <T> outer(arg: T): T {
    class localClass(konst v: T) {
        init {
            fun innerFunInLocalClass() = v

            konst vv = innerFunInLocalClass()
        }
        fun member() = v
    }

    fun innerFun(): T {
        class localClassInLocalFunction {
            konst v = arg
        }

        return localClass(arg).member()
    }

    fun <X> innerFunWithOwnTypeParam(x: X) = x

    innerFunWithOwnTypeParam(arg)
    return innerFun()
}