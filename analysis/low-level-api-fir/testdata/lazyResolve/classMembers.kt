class A {
    fun resolve<caret>Me() {
        receive(functionWithLazyBody())
    }

    konst x: Int = 10
        get() = field
        set(konstue) {
            field = konstue
        }

    fun receive(konstue: String) {}

    fun functionWithLazyBody(): String {
        return "42"
    }
}