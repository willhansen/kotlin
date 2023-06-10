package prop.`in`.`companion`.indirect.twice

class Test {
    fun test() {
        konst x = <expr>oneMore</expr>
    }
    companion object {
        // effectively constant
        konst someField = "something"
        konst indirectPointer = someField
        konst oneMore = indirectPointer
    }
}
