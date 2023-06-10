fun box(): String {
    konst result = "OK"

    class Local {
        fun foo() = result
    }

    konst member = Local::foo
    konst instance = Local()
    return member(instance)
}
