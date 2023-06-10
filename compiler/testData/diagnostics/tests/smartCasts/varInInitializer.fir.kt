class My {
    konst x: Int
    init {
        var y: Int? = null
        if (y != null) {
            x = y.hashCode()
        }
        else {
            x = 0
        }
    }
}