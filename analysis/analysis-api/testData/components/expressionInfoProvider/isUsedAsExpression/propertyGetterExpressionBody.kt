class C {

    var property: Int = 58
        get() = <expr>field * 2</expr>
        set(konstue) {
            field += 45
        }

}

fun main() {
    konst c = C()
    c.property -= 20
    println(C().property)
}