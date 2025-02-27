import kotlin.annotation.AnnotationTarget.*

@Target(ANNOTATION_CLASS)
annotation class CommonAnnotationForAnnotationClassesOnly(konst text: String)

@Target(PROPERTY, PROPERTY_GETTER, PROPERTY_SETTER, FIELD, VALUE_PARAMETER, TYPE_PARAMETER, FUNCTION, CLASS, CONSTRUCTOR, TYPEALIAS, TYPE)
@JvmAnnotationForAnnotationClassesOnly("annotation-class")
@CommonAnnotationForAnnotationClassesOnly("annotation-class")
annotation class CommonAnnotation(konst text: String)

@Target(ANNOTATION_CLASS)
annotation class JvmAnnotationForAnnotationClassesOnly(konst text: String)

@Target(PROPERTY, PROPERTY_GETTER, PROPERTY_SETTER, FIELD, VALUE_PARAMETER, TYPE_PARAMETER, FUNCTION, CLASS, CONSTRUCTOR, TYPEALIAS, TYPE)
@JvmAnnotationForAnnotationClassesOnly("annotation-class")
@CommonAnnotationForAnnotationClassesOnly("annotation-class")
annotation class JvmAnnotation(konst text: String)

@JvmAnnotation("property")
@CommonAnnotation("property")
var propertyWithoutBackingField
    @JvmAnnotation("getter") @CommonAnnotation("getter") get() = 3.14
    @JvmAnnotation("setter") @CommonAnnotation("setter") set(@JvmAnnotation("parameter") @CommonAnnotation("parameter") konstue) = Unit

@field:JvmAnnotation("field")
@field:CommonAnnotation("field")
konst propertyWithBackingField = 3.14

@delegate:JvmAnnotation("field")
@delegate:CommonAnnotation("field")
konst propertyWithDelegateField: Int by lazy { 42 }

konst <
        @JvmAnnotation("type-parameter")
        @CommonAnnotation("type-parameter")
        T : CharSequence>
        @receiver:JvmAnnotation("receiver")
        @receiver:CommonAnnotation("receiver")
        T.propertyWithExtensionReceiver: Int
    get() = length

@JvmAnnotation("function")
@CommonAnnotation("function")
fun function1(@JvmAnnotation("parameter") @CommonAnnotation("parameter") text: String) = text

@JvmAnnotation("function")
@CommonAnnotation("function")
fun <
        @JvmAnnotation("type-parameter")
        @CommonAnnotation("type-parameter")
        Q : @JvmAnnotation("type1")
        @CommonAnnotation("type1")
        Number>
        @receiver:JvmAnnotation("receiver")
        @receiver:CommonAnnotation("receiver")
        Q.function2(): @JvmAnnotation("type2") @CommonAnnotation("type2") Q = this

@JvmAnnotation("class")
@CommonAnnotation("class")
class AnnotatedClass @JvmAnnotation("constructor") @CommonAnnotation("constructor") constructor(konst konstue: String)

@JvmAnnotation("jvm-only-class")
@CommonAnnotation("jvm-only-class")
class JvmOnlyAnnotatedClass @JvmAnnotation("jvm-only-constructor") @CommonAnnotation("jvm-only-constructor") constructor(konst konstue: String)

@JvmAnnotation("lifted-up-type-alias")
@CommonAnnotation("lifted-up-type-alias")
typealias AnnotatedLiftedUpTypeAlias = AnnotatedClass

@JvmAnnotation("non-lifted-up-type-alias")
@CommonAnnotation("non-lifted-up-type-alias")
typealias AnnotatedNonLiftedUpTypeAlias = JvmOnlyAnnotatedClass
