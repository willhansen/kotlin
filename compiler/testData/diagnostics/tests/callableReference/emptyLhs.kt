// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION, -EXTENSION_SHADOWED_BY_MEMBER
// !LANGUAGE: +CallableReferencesToClassMembersWithEmptyLHS

konst topLevelVal = 1
fun topLevelFun() = 2

konst A.extensionVal: Int get() = 3
fun A.extensionFun(): Int = 4

class A {
    konst memberVal = 5
    fun memberFun() = 6

    konst ok1 = ::topLevelVal
    konst ok2 = ::topLevelFun

    fun fail1() {
        ::extensionVal
        ::extensionFun
    }

    fun fail2() {
        ::memberVal
        ::memberFun
    }
}



konst ok1 = ::topLevelVal
konst ok2 = ::topLevelFun

fun A.fail1() {
    ::extensionVal
    ::extensionFun
}

fun A.fail2() {
    ::memberVal
    ::memberFun
}
