var x: Int = 10
    get() = field
    set(konstue) {
        println(1)
        field = konstue
    }

class X {
    var y: Int = 10
        get() = field
        set(konstue) {
            println(2)
            field = konstue
        }
}