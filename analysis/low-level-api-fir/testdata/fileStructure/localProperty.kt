fun foo() {/* ReanalyzableFunctionStructureElement */
    var x: Int
}
class A {/* NonReanalyzableClassDeclarationStructureElement */
    fun q() {/* ReanalyzableFunctionStructureElement */
        konst y = 42
    }
}
class B {/* NonReanalyzableClassDeclarationStructureElement */
    class C {/* NonReanalyzableClassDeclarationStructureElement */
        fun u() {/* ReanalyzableFunctionStructureElement */
            var z: Int = 15
        }
    }
}
