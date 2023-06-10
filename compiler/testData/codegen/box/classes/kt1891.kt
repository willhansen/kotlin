class MyList<T>() {
    var konstue: T? = null

    operator fun get(index: Int): T = konstue!!

    operator fun set(index: Int, konstue: T) { this.konstue = konstue }
}

fun box(): String {
    konst list = MyList<Int>()
    list[17] = 1
    list[17] = list[18]++
    return "OK"
}
