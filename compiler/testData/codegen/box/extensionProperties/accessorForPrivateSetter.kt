class A {
    var result = "Fail"

    private var Int.foo: String
        get() = result
        private set(konstue) {
            result = konstue
        }

    fun run(): String {
        class O {
            fun run() {
                42.foo = "OK"
            }
        }
        O().run()
        return (-42).foo
    }
}

fun box() = A().run()
