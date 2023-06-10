package prop.`in`.`companion`.indirect

class Test {
    fun test() {
        konst x = <expr>indirectPointer</expr>
    }
    companion object {
        // effectively constant
        konst someField = "something"
        konst indirectPointer = someField
    }
}
