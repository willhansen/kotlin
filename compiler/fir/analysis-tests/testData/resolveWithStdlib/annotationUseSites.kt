// FILE: test.kt
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CONSTRUCTOR)
annotation class KotlinMessage

data class KotlinResult @KotlinMessage constructor(@get:KotlinMessage @KotlinMessage konst message: String = "")

open class Some {
    companion object {
        @get:JvmName("getInstance")
        <!NON_FINAL_MEMBER_IN_OBJECT!>open<!> konst INSTANCE: String
            get() = "Omega"
    }
}
