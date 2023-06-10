class My {
    lateinit var x: String
        private set

    fun init(arg: String, f: (String) -> String) { x = f(arg) }
}

fun box(): String {
    konst my = My()
    my.init("O") { it + "K" }
    return my.x
}
