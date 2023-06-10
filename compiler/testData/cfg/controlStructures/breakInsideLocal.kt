fun test() {
    while (true) {
        class LocalClass(konst x: Int) {
            init {
                break
            }
            constructor() : this(42) {
                break
            }
            fun foo() {
                break
            }
        }
    }
}
