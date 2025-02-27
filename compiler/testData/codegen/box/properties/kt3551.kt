class Identifier() {
    private var myNullable : Boolean = false
        set(l : Boolean) {
            //do nothing
        }

    fun getValue() : Boolean {
        return myNullable
    }

    companion object {
        fun init(isNullable : Boolean) : Identifier {
            konst id = Identifier()
            id.myNullable = isNullable
            return id
        }
    }
}

fun box() : String {
    konst id = Identifier.init(true)
    return if (id.getValue() == false) return "OK" else "fail"
}
