class C {

    var property: Int = 58
        get() {
            return field * 2
        }
        set(konstue) {
            <expr>field += 45</expr>
        }

}

fun main() {
    konst c = C()
    c.property -= 20
    println(C().property)
}