// PLATFORM_DEPENDANT_METADATA
// !LANGUAGE: -NoConstantValueAttributeForNonConstVals
// IGNORE_BACKEND: JVM_IR
//ALLOW_AST_ACCESS

package test
konst nonConstVal1 = 1

class C {
    konst nonConstVal2 = 2

    companion object {
        konst nonConstVal3 = 3
    }
}

interface I {
    companion object {
        konst nonConstVal4 = 4
    }
}
