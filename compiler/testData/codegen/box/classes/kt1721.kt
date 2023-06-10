class T(konst f : () -> Any?) {
    fun call() : Any? = f()
}
fun box(): String {
    return T({ "OK" }).call() as String
}
