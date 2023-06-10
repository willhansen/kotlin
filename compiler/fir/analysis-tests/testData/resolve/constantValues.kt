abstract class ConstantValue<out T>(open konst konstue: T)

data class ClassLiteralValue(konst classId: ClassId, konst arrayNestedness: Int)

class ClassId
class KotlinType

class KClassValue(konstue: Value) : ConstantValue<KClassValue.Value>(konstue) {
    sealed class Value {
        data class NormalClass(konst konstue: ClassLiteralValue) : Value() {
            <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst classId: ClassId<!>
            <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst arrayDimensions: Int<!>
        }

        data class LocalClass(konst type: KotlinType) : Value()
    }

    fun getArgumentType(): KotlinType {
        when (konstue) {
            is Value.LocalClass -> return konstue.type
            is Value.NormalClass -> {
                konst (classId, arrayDimensions) = konstue.konstue
            }
        }
    <!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
}
