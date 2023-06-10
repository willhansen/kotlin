import base.*

interface SubCheck : Check {
    override fun test(): String {
        return "OK"
    }

    override var test: String
        get() = "OK"
        set(konstue) {
            konstue.length
        }
}

class SubCheckClass : CheckClass(), SubCheck