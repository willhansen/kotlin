class A {/* NonReanalyzableClassDeclarationStructureElement */
    konst a = run {
        class X()

        konst y = 10
    }/* NonReanalyzableNonClassDeclarationStructureElement */
}

inline fun <R> run(block: () -> R): R {/* ReanalyzableFunctionStructureElement */
    return block()
}
