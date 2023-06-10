class C {

    <expr>var property: Int = 58
        get() {
            return field * 2
        }
        set(konstue) {
            field += 45
        }</expr>

}

fun main() {
    konst c = C()
    c.property -= 20
    println(C().property)
}