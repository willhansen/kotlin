interface AnnotationsOnParenthesizedTypes {
    fun B<(@A C)>.receiverArgument() {}

    fun parameter(a: (@A C)) {}

    fun parameterArgument(a: B<(@A C)>) {}

    fun returnValue(): (@A C)

    fun <T> returnTypeParameterValue(): (@A T)

    fun returnArgument(): B<(@A C)>

    konst lambdaType: (@A() (() -> C))

    konst lambdaParameter: ((@A C)) -> C

    konst lambdaReturnValue: () -> (@A C)

    konst lambdaReceiver: (@A C).() -> C

    konst lambdaParameterNP: (@A C) -> C
}

@Target(AnnotationTarget.TYPE, AnnotationTarget.TYPE_PARAMETER)
annotation class A

interface B<T>
interface C