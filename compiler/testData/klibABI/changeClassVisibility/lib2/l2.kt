@file:Suppress("FunctionName", "UNUSED_PARAMETER")

/**
 * Note:
 *
 * `fun *_konstueParameter()` accepts nullable type and always returns a string.
 *
 * `fun *_returnType()` has non-nullable return type but in fact always throws an instrance of [TestException]. This is to avoid induced
 * errors caused by instantition of the returned declaration itself.
 *
 * `fun *_anyReturnType` has [kotlin.Any] return type and always returns just created instance of the declaration.
 */

data class TestException(konst id: String) : Exception()
private fun fail(id: String): Nothing = throw TestException(id)

fun PublicTopLevelClass_konstueParameter(konstue: PublicTopLevelClass?): String = "PublicTopLevelClass"
fun PublicTopLevelClass_returnType(): PublicTopLevelClass = fail("PublicTopLevelClass")
fun PublicTopLevelClass_anyReturnType(): Any = PublicTopLevelClass()
fun PublicTopLevelClass_PublicToInternalNestedClass_konstueParameter(konstue: PublicTopLevelClass.PublicToInternalNestedClass?): String = "PublicTopLevelClass.PublicToInternalNestedClass"
fun PublicTopLevelClass_PublicToInternalNestedClass_returnType(): PublicTopLevelClass.PublicToInternalNestedClass = fail("PublicTopLevelClass.PublicToInternalNestedClass")
fun PublicTopLevelClass_PublicToInternalNestedClass_anyReturnType(): Any = PublicTopLevelClass.PublicToInternalNestedClass()
fun PublicTopLevelClass_PublicToProtectedNestedClass_konstueParameter(konstue: PublicTopLevelClass.PublicToProtectedNestedClass?): String = "PublicTopLevelClass.PublicToProtectedNestedClass"
fun PublicTopLevelClass_PublicToProtectedNestedClass_returnType(): PublicTopLevelClass.PublicToProtectedNestedClass = fail("PublicTopLevelClass.PublicToProtectedNestedClass")
fun PublicTopLevelClass_PublicToProtectedNestedClass_anyReturnType(): Any = PublicTopLevelClass.PublicToProtectedNestedClass()
fun PublicTopLevelClass_PublicToPrivateNestedClass_konstueParameter(konstue: PublicTopLevelClass.PublicToPrivateNestedClass?): String = "PublicTopLevelClass.PublicToPrivateNestedClass"
fun PublicTopLevelClass_PublicToPrivateNestedClass_returnType(): PublicTopLevelClass.PublicToPrivateNestedClass = fail("PublicTopLevelClass.PublicToPrivateNestedClass")
fun PublicTopLevelClass_PublicToPrivateNestedClass_anyReturnType(): Any = PublicTopLevelClass.PublicToPrivateNestedClass()
fun PublicTopLevelClass_PublicToInternalInnerClass_konstueParameter(konstue: PublicTopLevelClass.PublicToInternalInnerClass?): String = "PublicTopLevelClass.PublicToInternalInnerClass"
fun PublicTopLevelClass_PublicToInternalInnerClass_returnType(): PublicTopLevelClass.PublicToInternalInnerClass = fail("PublicTopLevelClass.PublicToInternalInnerClass")
fun PublicTopLevelClass_PublicToInternalInnerClass_anyReturnType(): Any = PublicTopLevelClass().PublicToInternalInnerClass()
fun PublicTopLevelClass_PublicToProtectedInnerClass_konstueParameter(konstue: PublicTopLevelClass.PublicToProtectedInnerClass?): String = "PublicTopLevelClass.PublicToProtectedInnerClass"
fun PublicTopLevelClass_PublicToProtectedInnerClass_returnType(): PublicTopLevelClass.PublicToProtectedInnerClass = fail("PublicTopLevelClass.PublicToProtectedInnerClass")
fun PublicTopLevelClass_PublicToProtectedInnerClass_anyReturnType(): Any = PublicTopLevelClass().PublicToProtectedInnerClass()
fun PublicTopLevelClass_PublicToPrivateInnerClass_konstueParameter(konstue: PublicTopLevelClass.PublicToPrivateInnerClass?): String = "PublicTopLevelClass.PublicToPrivateInnerClass"
fun PublicTopLevelClass_PublicToPrivateInnerClass_returnType(): PublicTopLevelClass.PublicToPrivateInnerClass = fail("PublicTopLevelClass.PublicToPrivateInnerClass")
fun PublicTopLevelClass_PublicToPrivateInnerClass_anyReturnType(): Any = PublicTopLevelClass().PublicToPrivateInnerClass()

fun PublicToInternalTopLevelClass_konstueParameter(konstue: PublicToInternalTopLevelClass?): String = "PublicToInternalTopLevelClass"
fun PublicToInternalTopLevelClass_returnType(): PublicToInternalTopLevelClass = fail("PublicToInternalTopLevelClass")
fun PublicToInternalTopLevelClass_anyReturnType(): Any = PublicToInternalTopLevelClass()
fun PublicToInternalTopLevelClass_PublicToInternalNestedClass_konstueParameter(konstue: PublicToInternalTopLevelClass.PublicToInternalNestedClass?): String = "PublicToInternalTopLevelClass.PublicToInternalNestedClass"
fun PublicToInternalTopLevelClass_PublicToInternalNestedClass_returnType(): PublicToInternalTopLevelClass.PublicToInternalNestedClass = fail("PublicToInternalTopLevelClass.PublicToInternalNestedClass")
fun PublicToInternalTopLevelClass_PublicToInternalNestedClass_anyReturnType(): Any = PublicToInternalTopLevelClass.PublicToInternalNestedClass()
fun PublicToInternalTopLevelClass_PublicToProtectedNestedClass_konstueParameter(konstue: PublicToInternalTopLevelClass.PublicToProtectedNestedClass?): String = "PublicToInternalTopLevelClass.PublicToProtectedNestedClass"
fun PublicToInternalTopLevelClass_PublicToProtectedNestedClass_returnType(): PublicToInternalTopLevelClass.PublicToProtectedNestedClass = fail("PublicToInternalTopLevelClass.PublicToProtectedNestedClass")
fun PublicToInternalTopLevelClass_PublicToProtectedNestedClass_anyReturnType(): Any = PublicToInternalTopLevelClass.PublicToProtectedNestedClass()
fun PublicToInternalTopLevelClass_PublicToPrivateNestedClass_konstueParameter(konstue: PublicToInternalTopLevelClass.PublicToPrivateNestedClass?): String = "PublicToInternalTopLevelClass.PublicToPrivateNestedClass"
fun PublicToInternalTopLevelClass_PublicToPrivateNestedClass_returnType(): PublicToInternalTopLevelClass.PublicToPrivateNestedClass = fail("PublicToInternalTopLevelClass.PublicToPrivateNestedClass")
fun PublicToInternalTopLevelClass_PublicToPrivateNestedClass_anyReturnType(): Any = PublicToInternalTopLevelClass.PublicToPrivateNestedClass()
fun PublicToInternalTopLevelClass_PublicToInternalInnerClass_konstueParameter(konstue: PublicToInternalTopLevelClass.PublicToInternalInnerClass?): String = "PublicToInternalTopLevelClass.PublicToInternalInnerClass"
fun PublicToInternalTopLevelClass_PublicToInternalInnerClass_returnType(): PublicToInternalTopLevelClass.PublicToInternalInnerClass = fail("PublicToInternalTopLevelClass.PublicToInternalInnerClass")
fun PublicToInternalTopLevelClass_PublicToInternalInnerClass_anyReturnType(): Any = PublicToInternalTopLevelClass().PublicToInternalInnerClass()
fun PublicToInternalTopLevelClass_PublicToProtectedInnerClass_konstueParameter(konstue: PublicToInternalTopLevelClass.PublicToProtectedInnerClass?): String = "PublicToInternalTopLevelClass.PublicToProtectedInnerClass"
fun PublicToInternalTopLevelClass_PublicToProtectedInnerClass_returnType(): PublicToInternalTopLevelClass.PublicToProtectedInnerClass = fail("PublicToInternalTopLevelClass.PublicToProtectedInnerClass")
fun PublicToInternalTopLevelClass_PublicToProtectedInnerClass_anyReturnType(): Any = PublicToInternalTopLevelClass().PublicToProtectedInnerClass()
fun PublicToInternalTopLevelClass_PublicToPrivateInnerClass_konstueParameter(konstue: PublicToInternalTopLevelClass.PublicToPrivateInnerClass?): String = "PublicToInternalTopLevelClass.PublicToPrivateInnerClass"
fun PublicToInternalTopLevelClass_PublicToPrivateInnerClass_returnType(): PublicToInternalTopLevelClass.PublicToPrivateInnerClass = fail("PublicToInternalTopLevelClass.PublicToPrivateInnerClass")
fun PublicToInternalTopLevelClass_PublicToPrivateInnerClass_anyReturnType(): Any = PublicToInternalTopLevelClass().PublicToPrivateInnerClass()

fun PublicToPrivateTopLevelClass_konstueParameter(konstue: PublicToPrivateTopLevelClass?): String = "PublicToPrivateTopLevelClass"
fun PublicToPrivateTopLevelClass_returnType(): PublicToPrivateTopLevelClass = fail("PublicToPrivateTopLevelClass")
fun PublicToPrivateTopLevelClass_anyReturnType(): Any = PublicToPrivateTopLevelClass()
fun PublicToPrivateTopLevelClass_PublicToInternalNestedClass_konstueParameter(konstue: PublicToPrivateTopLevelClass.PublicToInternalNestedClass?): String = "PublicToPrivateTopLevelClass.PublicToInternalNestedClass"
fun PublicToPrivateTopLevelClass_PublicToInternalNestedClass_returnType(): PublicToPrivateTopLevelClass.PublicToInternalNestedClass = fail("PublicToPrivateTopLevelClass.PublicToInternalNestedClass")
fun PublicToPrivateTopLevelClass_PublicToInternalNestedClass_anyReturnType(): Any = PublicToPrivateTopLevelClass.PublicToInternalNestedClass()
fun PublicToPrivateTopLevelClass_PublicToProtectedNestedClass_konstueParameter(konstue: PublicToPrivateTopLevelClass.PublicToProtectedNestedClass?): String = "PublicToPrivateTopLevelClass.PublicToProtectedNestedClass"
fun PublicToPrivateTopLevelClass_PublicToProtectedNestedClass_returnType(): PublicToPrivateTopLevelClass.PublicToProtectedNestedClass = fail("PublicToPrivateTopLevelClass.PublicToProtectedNestedClass")
fun PublicToPrivateTopLevelClass_PublicToProtectedNestedClass_anyReturnType(): Any = PublicToPrivateTopLevelClass.PublicToProtectedNestedClass()
fun PublicToPrivateTopLevelClass_PublicToPrivateNestedClass_konstueParameter(konstue: PublicToPrivateTopLevelClass.PublicToPrivateNestedClass?): String = "PublicToPrivateTopLevelClass.PublicToPrivateNestedClass"
fun PublicToPrivateTopLevelClass_PublicToPrivateNestedClass_returnType(): PublicToPrivateTopLevelClass.PublicToPrivateNestedClass = fail("PublicToPrivateTopLevelClass.PublicToPrivateNestedClass")
fun PublicToPrivateTopLevelClass_PublicToPrivateNestedClass_anyReturnType(): Any = PublicToPrivateTopLevelClass.PublicToPrivateNestedClass()
fun PublicToPrivateTopLevelClass_PublicToInternalInnerClass_konstueParameter(konstue: PublicToPrivateTopLevelClass.PublicToInternalInnerClass?): String = "PublicToPrivateTopLevelClass.PublicToInternalInnerClass"
fun PublicToPrivateTopLevelClass_PublicToInternalInnerClass_returnType(): PublicToPrivateTopLevelClass.PublicToInternalInnerClass = fail("PublicToPrivateTopLevelClass.PublicToInternalInnerClass")
fun PublicToPrivateTopLevelClass_PublicToInternalInnerClass_anyReturnType(): Any = PublicToPrivateTopLevelClass().PublicToInternalInnerClass()
fun PublicToPrivateTopLevelClass_PublicToProtectedInnerClass_konstueParameter(konstue: PublicToPrivateTopLevelClass.PublicToProtectedInnerClass?): String = "PublicToPrivateTopLevelClass.PublicToProtectedInnerClass"
fun PublicToPrivateTopLevelClass_PublicToProtectedInnerClass_returnType(): PublicToPrivateTopLevelClass.PublicToProtectedInnerClass = fail("PublicToPrivateTopLevelClass.PublicToProtectedInnerClass")
fun PublicToPrivateTopLevelClass_PublicToProtectedInnerClass_anyReturnType(): Any = PublicToPrivateTopLevelClass().PublicToProtectedInnerClass()
fun PublicToPrivateTopLevelClass_PublicToPrivateInnerClass_konstueParameter(konstue: PublicToPrivateTopLevelClass.PublicToPrivateInnerClass?): String = "PublicToPrivateTopLevelClass.PublicToPrivateInnerClass"
fun PublicToPrivateTopLevelClass_PublicToPrivateInnerClass_returnType(): PublicToPrivateTopLevelClass.PublicToPrivateInnerClass = fail("PublicToPrivateTopLevelClass.PublicToPrivateInnerClass")
fun PublicToPrivateTopLevelClass_PublicToPrivateInnerClass_anyReturnType(): Any = PublicToPrivateTopLevelClass().PublicToPrivateInnerClass()

class PublicTopLevelClassInheritor : PublicTopLevelClass() { override fun toString() = "PublicTopLevelClassInheritor" }
class PublicToInternalTopLevelClassInheritor : PublicToInternalTopLevelClass() { override fun toString() = "PublicToInternalTopLevelClassInheritor" }
class PublicToPrivateTopLevelClassInheritor : PublicToPrivateTopLevelClass() { override fun toString() = "PublicToPrivateTopLevelClassInheritor" }
