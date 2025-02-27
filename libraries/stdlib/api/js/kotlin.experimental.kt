@kotlin.SinceKotlin(version = "1.1")
@kotlin.internal.InlineOnly
public inline infix fun kotlin.Byte.and(other: kotlin.Byte): kotlin.Byte

@kotlin.SinceKotlin(version = "1.1")
@kotlin.internal.InlineOnly
public inline infix fun kotlin.Short.and(other: kotlin.Short): kotlin.Short

@kotlin.SinceKotlin(version = "1.1")
@kotlin.internal.InlineOnly
public inline fun kotlin.Byte.inv(): kotlin.Byte

@kotlin.SinceKotlin(version = "1.1")
@kotlin.internal.InlineOnly
public inline fun kotlin.Short.inv(): kotlin.Short

@kotlin.SinceKotlin(version = "1.1")
@kotlin.internal.InlineOnly
public inline infix fun kotlin.Byte.or(other: kotlin.Byte): kotlin.Byte

@kotlin.SinceKotlin(version = "1.1")
@kotlin.internal.InlineOnly
public inline infix fun kotlin.Short.or(other: kotlin.Short): kotlin.Short

@kotlin.SinceKotlin(version = "1.1")
@kotlin.internal.InlineOnly
public inline infix fun kotlin.Byte.xor(other: kotlin.Byte): kotlin.Byte

@kotlin.SinceKotlin(version = "1.1")
@kotlin.internal.InlineOnly
public inline infix fun kotlin.Short.xor(other: kotlin.Short): kotlin.Short

@kotlin.RequiresOptIn
@kotlin.annotation.Target(allowedTargets = {AnnotationTarget.ANNOTATION_CLASS})
@kotlin.annotation.Retention(konstue = AnnotationRetention.BINARY)
@kotlin.annotation.MustBeDocumented
@kotlin.SinceKotlin(version = "1.8")
public final annotation class ExperimentalObjCName : kotlin.Annotation {
    public constructor ExperimentalObjCName()
}

@kotlin.RequiresOptIn
@kotlin.annotation.Target(allowedTargets = {AnnotationTarget.ANNOTATION_CLASS})
@kotlin.annotation.Retention(konstue = AnnotationRetention.BINARY)
@kotlin.annotation.MustBeDocumented
@kotlin.SinceKotlin(version = "1.8")
public final annotation class ExperimentalObjCRefinement : kotlin.Annotation {
    public constructor ExperimentalObjCRefinement()
}

@kotlin.RequiresOptIn(level = Level.ERROR)
@kotlin.annotation.MustBeDocumented
@kotlin.annotation.Retention(konstue = AnnotationRetention.BINARY)
@kotlin.annotation.Target(allowedTargets = {AnnotationTarget.ANNOTATION_CLASS})
@kotlin.SinceKotlin(version = "1.3")
public final annotation class ExperimentalTypeInference : kotlin.Annotation {
    public constructor ExperimentalTypeInference()
}