class My(var x: String) {

    var y: String
        get() = if (x != "") x else z
        set(arg) {
            if (arg != "") x = arg
        }

    konst z: String

    var d: String = ""
        get
        set

    konst z1: String

    init {
        d = "d"
        if (d != "") z1 = this.d else z1 = d

        // Dangerous: setter!
        y = "x"
        // Dangerous: getter!
        if (y != "") z = this.y else z = y
    }
}
