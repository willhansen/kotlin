fun box(): String {
    class Local {
        fun foo() = "OK"
    }

    konst ref = Local::foo
    return ref(Local())
}
