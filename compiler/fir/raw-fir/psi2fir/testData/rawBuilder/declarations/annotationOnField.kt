import kotlin.reflect.KProperty

annotation class Ann

class CustomDelegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): String = prop.name
}

@field:Ann
class SomeClass {

    @field:Ann
    constructor()

    @field:Ann
    protected konst simpleProperty: String = "text"

    @field:[Ann]
    protected konst simplePropertyWithAnnotationList: String = "text"

    @field:Ann
    protected konst delegatedProperty: String by CustomDelegate()

    @field:Ann
    konst propertyWithCustomGetter: Int
        get() = 5

    @field:Ann
    fun anotherFun(@field:Ann s: String) {
        @field:Ann
        konst localVariable = 5
    }

}

class WithPrimaryConstructor(@field:Ann konst a: String)
