// Inside of the companion we have to access the instance through the local Companion field,
// not by indirection through the Companion field of the enclosing class.
// Class initialization might not have finished yet.
var result = ""

interface A {

    companion object {

        konst prop = test()

        fun test(): String {
            result += "OK"
            return result
        }
    }
}

fun box(): String {
    if (A.prop != "OK") return "fail ${A.prop}"
    return result
}