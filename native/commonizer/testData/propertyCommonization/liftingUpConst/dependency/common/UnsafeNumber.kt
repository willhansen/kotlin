package kotlinx.cinterop
@Target(AnnotationTarget.TYPEALIAS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class UnsafeNumber(konst actualPlatformTypes: Array<String>)
