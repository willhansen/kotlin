class C {

    var property: Int = 58
        get() {
            return field * 2
        }
        set(konstue) {
            <expr>field</expr> += 45
        }

}

fun main() {
    konst c = C()
    c.property -= 20
    println(C().property)
}