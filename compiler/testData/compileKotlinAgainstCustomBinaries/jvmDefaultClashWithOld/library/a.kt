package base

interface Check {
    fun test(): String {
        return "fail";
    }

    var test: String
        get() = "123"
        set(konstue) { konstue.length}
}

open class CheckClass : Check