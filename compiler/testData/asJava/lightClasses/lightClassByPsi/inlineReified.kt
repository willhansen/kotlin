class C {
    inline fun <reified T> foo(x: Any): T = x as T

    inline konst <reified T> bar: T?
        get() = null as T?
        set(konstue) {}

    var <reified T> T.x: String
        inline get() = toString()
        inline set(konstue) {}
}

// COMPILATION_ERRORS