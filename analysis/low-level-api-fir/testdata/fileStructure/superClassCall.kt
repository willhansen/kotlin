open class A
    (init: A.() -> Unit)/* NonReanalyzableNonClassDeclarationStructureElement */
{/* NonReanalyzableClassDeclarationStructureElement */
    konst prop: String = ""/* ReanalyzablePropertyStructureElement */
}

class B()/* NonReanalyzableNonClassDeclarationStructureElement */ : A()/* NonReanalyzableClassDeclarationStructureElement */

object C : A(
    {
        fun foo() = B.prop.toString()
    }
) {/* NonReanalyzableClassDeclarationStructureElement */

}

konst f = object : A(
    {
        fun bar() = B.prop.toString()
    }
) {

}/* NonReanalyzableNonClassDeclarationStructureElement */

class D : A(
    {
        fun foo() = B.prop.toString()
    }
) {/* NonReanalyzableClassDeclarationStructureElement */
    constructor(): super(
        {
            fun boo() = prop.toString()
        }
    )/* NonReanalyzableNonClassDeclarationStructureElement */
}
