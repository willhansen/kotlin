public class AnnotationsOnParenthesizedTypes {
    fun B<(@A C)>.receiverArgument() {}

    fun parameter(a: (@A C)) {}

    fun parameterArgument(a: B<(@A C)>) {}

    fun returnValue(): (@A C) = null!!

    fun <T> returnTypeParameterValue(): (@A T) = null!!

    fun returnArgument(): B<(@A C)> = null!!

    konst lambdaType: (@A() (() -> C)) = null!!

    konst lambdaParameter: ((@A C)) -> C = null!!

    konst lambdaReturnValue: () -> (@A C) = null!!

    konst lambdaReceiver: (@A C).() -> C = null!!

    konst lambdaTypeWithNullableReceiver: (@A C)?.() -> C = null!!
}

@Target(AnnotationTarget.TYPE, AnnotationTarget.TYPE_PARAMETER)
annotation class A

interface B<T>
interface C