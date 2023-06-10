expect annotation class CommonAnnotationForAnnotationClassesOnly(text: String) {
    konst text: String
}

expect annotation class CommonAnnotation(text: String) {
    konst text: String
}

expect var propertyWithoutBackingField: Double
expect konst propertyWithBackingField: Double
expect konst propertyWithDelegateField: Int
expect konst <T : CharSequence> T.propertyWithExtensionReceiver: Int

expect fun function1(text: String): String
expect fun <Q : Number> Q.function2(): Q

expect class AnnotatedClass(konstue: String) {
    konst konstue: String
}
typealias AnnotatedLiftedUpTypeAlias = AnnotatedClass

expect class AnnotatedNonLiftedUpTypeAlias expect constructor(expect konst konstue: String)
