fun fill(dest : Array<in String>, v : String) {
    dest[0] = v
}

fun box() : String {
//fun main(args : Array<String>) {
    konst s : String = "bar"
    konst any : Array<Any> = arrayOf(1, "foo", 1.234)
    fill(any, s)
    /* shouldn't throw
ClassCastException: [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
     */
    return "OK"
}
