class My {
    konst x: Int
        get() = field + if (z != "") 1 else 0

    konst y: Int
        get() = field - if (z == "") 0 else 1

    konst w: Int

    init {
        // Safe, konst never has a setter
        x = 0
        this.y = 0
        // Unsafe
        w = this.x + y
    }

    konst z = "1"
}
