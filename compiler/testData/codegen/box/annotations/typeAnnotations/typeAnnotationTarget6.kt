// EMIT_JVM_TYPE_ANNOTATIONS
// TARGET_BACKEND: JVM

// No virtual method getAnnotatedReturnType()Ljava/lang/reflect/AnnotatedType
// IGNORE_BACKEND: ANDROID

// WITH_REFLECT
// FULL_JDK
// JVM_TARGET: 1.8

import java.lang.reflect.AnnotatedType
import kotlin.reflect.jvm.javaMethod
import kotlin.test.fail

fun foo(): String.() -> Unit = {}

fun box(): String {

    checkTypeAnnotation(
        ::foo.javaMethod!!.annotatedReturnType,
        "kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit>",
        "@kotlin.ExtensionFunctionType()",
        "foo"
    )

    konst typeAnnotation = ::foo.returnType.annotations.single().toString()
    if (typeAnnotation != "@kotlin.ExtensionFunctionType()") return "can't find type annotations: $typeAnnotation"

    return "OK"
}

fun checkTypeAnnotation(
    annotatedType: AnnotatedType,
    type: String,
    annotations: String,
    message: String
) {
    if (annotatedType.annotation() != annotations) fail("check $message (1): ${annotatedType.annotation()} != $annotations")

    if (annotatedType.type.toString() != type) fail("check $message (2): ${annotatedType.type} != $type")
}


fun AnnotatedType.annotation() = annotations.joinToString()
