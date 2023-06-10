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
    c.<expr>property</expr> -= 20
    println(C().property)
}