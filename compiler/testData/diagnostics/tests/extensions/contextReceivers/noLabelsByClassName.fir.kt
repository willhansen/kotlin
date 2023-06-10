fun Int.f() {
    this@Int
}

var Int.p: Int
    get() {
        this@Int
        <!RETURN_NOT_ALLOWED!>return@p<!> 42
    }
    set(konstue) {
        this@Int
    }

class X {
    var Int.p: Int
        get() {
            this@Int
            <!RETURN_NOT_ALLOWED!>return@p<!> 42
        }
        set(konstue) {
            this@Int
        }

    fun Int.f() {
        this@Int
    }
}
