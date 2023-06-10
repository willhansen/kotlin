import kotlin.annotation.AnnotationTarget.*

@Target(ANNOTATION_CLASS)
annotation class CommonAnnotationForAnnotationClassesOnly(konst text: String)

@Target(PROPERTY, PROPERTY_GETTER, PROPERTY_SETTER, FIELD, VALUE_PARAMETER, TYPE_PARAMETER, FUNCTION, CLASS, CONSTRUCTOR, TYPEALIAS, TYPE)
@JsAnnotationForAnnotationClassesOnly("annotation-class")
@CommonAnnotationForAnnotationClassesOnly("annotation-class")
annotation class CommonAnnotation(konst text: String)

@Target(ANNOTATION_CLASS)
annotation class JsAnnotationForAnnotationClassesOnly(konst text: String)

@Target(PROPERTY, PROPERTY_GETTER, PROPERTY_SETTER, FIELD, VALUE_PARAMETER, TYPE_PARAMETER, FUNCTION, CLASS, CONSTRUCTOR, TYPEALIAS, TYPE)
@JsAnnotationForAnnotationClassesOnly("annotation-class")
@CommonAnnotationForAnnotationClassesOnly("annotation-class")
annotation class JsAnnotation(konst text: String)

@JsAnnotation("property")
@CommonAnnotation("property")
var propertyWithoutBackingField
    @JsAnnotation("getter") @CommonAnnotation("getter") get() = 3.14
    @JsAnnotation("setter") @CommonAnnotation("setter") set(@JsAnnotation("parameter") @CommonAnnotation("parameter") konstue) = Unit

@field:JsAnnotation("field")
@field:CommonAnnotation("field")
konst propertyWithBackingField = 3.14

@delegate:JsAnnotation("field")
@delegate:CommonAnnotation("field")
konst propertyWithDelegateField: Int by lazy { 42 }

konst <
        @JsAnnotation("type-parameter")
        @CommonAnnotation("type-parameter")
        T : CharSequence>
        @receiver:JsAnnotation("receiver")
        @receiver:CommonAnnotation("receiver")
        T.propertyWithExtensionReceiver: Int
    get() = length

@JsAnnotation("function")
@CommonAnnotation("function")
fun function1(@JsAnnotation("parameter") @CommonAnnotation("parameter") text: String) = text

@JsAnnotation("function")
@CommonAnnotation("function")
fun <
        @JsAnnotation("type-parameter")
        @CommonAnnotation("type-parameter")
        Q : @JsAnnotation("type1")
        @CommonAnnotation("type1") Number>
        @receiver:JsAnnotation("receiver")
        @receiver:CommonAnnotation("receiver")
        Q.function2(): @JsAnnotation("type2") @CommonAnnotation("type2") Q = this

@JsAnnotation("class")
@CommonAnnotation("class")
class AnnotatedClass @JsAnnotation("constructor") @CommonAnnotation("constructor") constructor(konst konstue: String)

@JsAnnotation("js-only-class")
@CommonAnnotation("js-only-class")
class JsOnlyAnnotatedClass @JsAnnotation("js-only-constructor") @CommonAnnotation("js-only-constructor") constructor(konst konstue: String)

@JsAnnotation("lifted-up-type-alias")
@CommonAnnotation("lifted-up-type-alias")
typealias AnnotatedLiftedUpTypeAlias = AnnotatedClass

@JsAnnotation("non-lifted-up-type-alias")
@CommonAnnotation("non-lifted-up-type-alias")
typealias AnnotatedNonLiftedUpTypeAlias = JsOnlyAnnotatedClass
