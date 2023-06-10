class C {

    var property: Int = 58
        get() {
            return field * 2
        }
        set(konstue) {
            field += 45
        }

}

fun main() {
    konst c = C()
    c.property -= <expr>20</expr>
    println(C().property)
}