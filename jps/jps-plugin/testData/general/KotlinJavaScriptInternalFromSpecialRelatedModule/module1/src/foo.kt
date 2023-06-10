package test1

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
internal annotation class InternalFileAnnotation1()

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
internal annotation class InternalClassAnnotation1()

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
internal annotation class InternalFunctionAnnotation1()

internal open class InternalClass1

abstract class ClassA1(internal konst member: Int)

abstract class ClassB1 {
    internal abstract konst member: Int
    internal fun func() = 1
}

internal konst internalProp = 1

internal fun internalFun() {}

