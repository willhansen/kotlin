class MyClass {
    companion object {
        var prop: Int = 4
            @JvmStatic
            set(konstue) {
                field = konstue
            }

            get() = field

        @get:JvmStatic
        var prop2: String = ""
            set(konstue) {
                field = konstue
            }
    }
}
