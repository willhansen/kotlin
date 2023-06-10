// FIR_IDENTICAL
import kotlin.reflect.KClass

const konst CONST = 1
fun foo() = 1
konst nonConst = foo()

annotation class ValidAnn(
    konst p1: Int = 1 + CONST,
    konst p2: String = "",
    konst p3: KClass<*> = String::class,
    konst p4: IntArray = intArrayOf(1, 2, 3),
    konst p5: Array<String> = arrayOf("abc"),
    konst p6: Array<KClass<*>> = arrayOf(Int::class)
)

konst nonConstKClass = String::class

annotation class InkonstidAnn(
    konst p1: Int = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>foo()<!>,
    konst p2: Int = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>nonConst<!>,
    konst p3: KClass<*> = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>nonConstKClass<!>
)
