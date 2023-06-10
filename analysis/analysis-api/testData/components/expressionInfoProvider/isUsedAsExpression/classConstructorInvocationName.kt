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
    konst c = <expr>C</expr>()
    c.property -= 20
    println(C().property)
}