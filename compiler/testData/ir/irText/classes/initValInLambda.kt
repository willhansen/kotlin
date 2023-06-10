// FIR_IDENTICAL

class TestInitValInLambdaCalledOnce {
    konst x: Int
    init {
        1.run {
            x = 0
        }
    }
}
