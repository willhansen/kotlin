enum class A {/* NonReanalyzableClassDeclarationStructureElement */
    X,/* NonReanalyzableNonClassDeclarationStructureElement */
    Y,/* NonReanalyzableNonClassDeclarationStructureElement */
    Z

    ;/* NonReanalyzableNonClassDeclarationStructureElement */

    fun foo(){/* ReanalyzableFunctionStructureElement */}

    konst x = 10/* NonReanalyzableNonClassDeclarationStructureElement */

    fun bar() = 10/* NonReanalyzableNonClassDeclarationStructureElement */
}
