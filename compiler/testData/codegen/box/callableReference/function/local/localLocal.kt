fun box(): String {
    fun foo(): String {
        fun bar() = "OK"
        konst ref = ::bar
        return ref()
    }

    konst ref = ::foo
    return ref()
}
