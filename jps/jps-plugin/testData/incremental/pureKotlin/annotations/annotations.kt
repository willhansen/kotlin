package test

annotation class Anno

@Anno fun f() {
}

@Anno konst v1 = ""

var v2: String
    get() = ""
    @Anno set(konstue) {
    }
