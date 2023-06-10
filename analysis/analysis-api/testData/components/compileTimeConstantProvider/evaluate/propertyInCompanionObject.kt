package prop.`in`.`companion`

class Test {
    fun test() {
        konst x = <expr>someField</expr>
    }
    companion object {
        // effectively constant
        konst someField = "something"
    }
}
