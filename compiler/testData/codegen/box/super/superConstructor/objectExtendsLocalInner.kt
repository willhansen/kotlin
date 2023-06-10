fun box(): String {
    konst capture = "O"

    class Local {
        konst captured = capture

        open inner class Inner(konst d: Double = -1.0, konst s: String, vararg konst y: Int) {
            open fun result() = "Fail"
        }

        konst obj = object : Inner(s = "K") {
            override fun result() = capture + s
        }
    }

    return Local().obj.result()
}
