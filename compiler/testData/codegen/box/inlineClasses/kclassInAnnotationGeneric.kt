// WITH_REFLECT
// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter
package test

import kotlin.reflect.KClass

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICInt<T: Int>(konst i: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICIntArray(konst i: IntArray)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICIntN<T: Int?>(konst i: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICIntN2<T: Int>(konst i: T?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICIntNArray(konst i: Array<Int?>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICAny<T: Any>(konst a: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICAnyArray(konst a: Array<Any>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICAnyNArray(konst a: Array<Any?>)

annotation class Ann(konst c: KClass<*>)
annotation class AnnArray(konst c: Array<KClass<*>>)

@Ann(ICInt::class)
@AnnArray([ICInt::class])
class CInt

@Ann(ICIntArray::class)
@AnnArray([ICIntArray::class])
class CIntArray

@Ann(ICIntN::class)
@AnnArray([ICIntN::class])
class CIntN

@Ann(ICIntN2::class)
@AnnArray([ICIntN2::class])
class CIntN2

@Ann(ICIntNArray::class)
@AnnArray([ICIntNArray::class])
class CIntNArray

@Ann(ICAny::class)
@AnnArray([ICAny::class])
class CAny

@Ann(ICAnyArray::class)
@AnnArray([ICAnyArray::class])
class CAnyArray

@Ann(Result::class)
@AnnArray([Result::class])
class CResult

@Ann(ICAnyNArray::class)
@AnnArray([ICAnyNArray::class])
class CAnyNArray

fun box(): String {
    var klass = (CInt::class.annotations.first() as Ann).c.toString()
    if (klass != "class test.ICInt") return "Expected class test.ICInt, got $klass"

    klass = (CIntArray::class.annotations.first() as Ann).c.toString()
    if (klass != "class test.ICIntArray") return "Expected class test.ICIntArray, got $klass"

    klass = (CIntN::class.annotations.first() as Ann).c.toString()
    if (klass != "class test.ICIntN") return "Expected class test.ICIntN, got $klass"

    klass = (CIntN2::class.annotations.first() as Ann).c.toString()
    if (klass != "class test.ICIntN2") return "Expected class test.ICIntN2, got $klass"

    klass = (CIntNArray::class.annotations.first() as Ann).c.toString()
    if (klass != "class test.ICIntNArray") return "Expected class test.ICIntNArray, got $klass"

    klass = (CAny::class.annotations.first() as Ann).c.toString()
    if (klass != "class test.ICAny") return "Expected class test.ICAny, got $klass"

    klass = (CAnyArray::class.annotations.first() as Ann).c.toString()
    if (klass != "class test.ICAnyArray") return "Expected class test.ICAnyArray, got $klass"

    klass = (CResult::class.annotations.first() as Ann).c.toString()
    if (klass != "class kotlin.Result") return "Expected class kotlin.Result, got $klass"

    klass = (CAnyNArray::class.annotations.first() as Ann).c.toString()
    if (klass != "class test.ICAnyNArray") return "Expected class test.ICAnyNArray, got $klass"


    klass = (CInt::class.annotations.last() as AnnArray).c[0].toString()
    if (klass != "class test.ICInt") return "Expected class test.ICInt, got $klass"

    klass = (CIntArray::class.annotations.last() as AnnArray).c[0].toString()
    if (klass != "class test.ICIntArray") return "Expected class test.ICIntArray, got $klass"

    klass = (CIntN::class.annotations.last() as AnnArray).c[0].toString()
    if (klass != "class test.ICIntN") return "Expected class test.ICIntN, got $klass"

    klass = (CIntN2::class.annotations.last() as AnnArray).c[0].toString()
    if (klass != "class test.ICIntN2") return "Expected class test.ICIntN2, got $klass"

    klass = (CIntNArray::class.annotations.last() as AnnArray).c[0].toString()
    if (klass != "class test.ICIntNArray") return "Expected class test.ICIntNArray, got $klass"

    klass = (CAny::class.annotations.last() as AnnArray).c[0].toString()
    if (klass != "class test.ICAny") return "Expected class test.ICAny, got $klass"

    klass = (CAnyArray::class.annotations.last() as AnnArray).c[0].toString()
    if (klass != "class test.ICAnyArray") return "Expected class test.ICAnyArray, got $klass"

    klass = (CResult::class.annotations.last() as AnnArray).c[0].toString()
    if (klass != "class kotlin.Result") return "Expected class kotlin.Result, got $klass"

    klass = (CAnyNArray::class.annotations.last() as AnnArray).c[0].toString()
    if (klass != "class test.ICAnyNArray") return "Expected class test.ICAnyNArray, got $klass"

    return "OK"
}