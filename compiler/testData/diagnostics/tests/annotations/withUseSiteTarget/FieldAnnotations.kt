// FIR_IDENTICAL
import kotlin.reflect.KProperty

annotation class Ann

class CustomDelegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): String = prop.name
}

<!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@field:Ann<!>
class SomeClass {

    <!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@field:Ann<!>
    constructor()

    @field:Ann
    protected konst simpleProperty: String = "text"

    @field:[Ann]
    protected konst simplePropertyWithAnnotationList: String = "text"

    <!INAPPLICABLE_TARGET_PROPERTY_HAS_NO_BACKING_FIELD!>@field:Ann<!>
    protected konst delegatedProperty: String by CustomDelegate()

    <!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@field:Ann<!>
    konst propertyWithCustomGetter: Int
        get() = 5

    <!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@field:Ann<!>
    fun anotherFun(<!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@field:Ann<!> s: String) {
        <!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@field:Ann<!>
        konst localVariable = 5
    }

}

class WithPrimaryConstructor(@field:Ann konst a: String)
