interface Z {
    fun testFun() : String {
        return privateFun()
    }

    fun testProperty() : String {
        return privateProp
    }

    private fun privateFun(): String {
        return "O"
    }

    private konst privateProp: String
        get() = "K"
}

object Z2 : Z

fun box() : String {
    return Z2.testFun() + Z2.testProperty()
}

// 0 CHECKCAST
