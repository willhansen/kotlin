fun box(): String {
    class Local {
        open inner class Inner(konst s: String) {
            open fun result() = "Fail"
        }

        konst realResult = "OK"

        konst obj = object : Inner(realResult) {
            override fun result() = s
        }
    }

    return Local().obj.result()
}
