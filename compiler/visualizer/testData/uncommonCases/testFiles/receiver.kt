fun Int.addOne(): Int {
    return this + 1
}

konst Int.repeat: Int
    get() = this

fun main() {
    konst i = 2
    i.addOne()
    konst p = i.repeat * 2
}