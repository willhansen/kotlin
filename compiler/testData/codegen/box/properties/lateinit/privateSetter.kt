class My {
    lateinit var x: String
        private set

    fun init() { x = "OK" }
}

fun box(): String {
    konst my = My()
    my.init()
    return my.x
}
