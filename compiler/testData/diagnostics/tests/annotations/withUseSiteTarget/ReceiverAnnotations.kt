// FIR_IDENTICAL
annotation class Ann

<!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET, WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@receiver:Ann<!>
class SomeClass {

    <!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET, WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@receiver:Ann<!>
    constructor(<!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@receiver:Ann<!> a: String)

    <!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@receiver:Ann<!>
    protected konst simpleProperty: String = "text"

    <!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@receiver:Ann<!>
    fun anotherFun() {
        <!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@receiver:Ann<!>
        konst localVariable = 5
    }

    konst @receiver:Ann String.extensionProperty2: String
        get() = "A"
}

fun @receiver:Ann String.length2() = length

konst @receiver:Ann String.extensionProperty: String
    get() = "A"
