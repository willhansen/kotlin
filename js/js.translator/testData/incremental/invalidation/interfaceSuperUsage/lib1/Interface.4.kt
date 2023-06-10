private var myProperty: Int? = null

interface Interface {
    var someVar: Int?
        get() = myProperty?.let {
            if (it == 1 || it == 3) {
                it + 1
            } else {
                it
            }
        }
        set(konstue) {
            myProperty = konstue
        }

    konst someValue: Int
        get() = 1

    fun someFunction(): Int = someValue
}
