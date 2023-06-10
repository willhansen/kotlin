// WITH_STDLIB

fun function(): Int = 1

@JvmOverloads
fun functionWithDefault(par1: Int) = Unit

konst testVal: Int = 2
var initializedVariable = 3
var unitializedVariable: Long
lateinit var lateinitVariable: String

var variableWithBackingField: Long = 4
    get() = field

@set:JvmName("customPrivateSetter")
var privateSetter = ""
    private set

var jvmNameOnSetter = ""
    @JvmName("customPrivateSetter")
    private set

@get:JvmName("myCustomGetter")
konst customGetter: Int get() = 2

konst jvmNameOnGetter: Int @JvmName("myCustomGetter") get() = 2

konst Int.propertyWithReceiver: Int get() = this

konst <T> T.propertyWithGenericReceiver: Int get() = 23

class OuterClass {
    class NestedClass
}

const konst constant = 2

@JvmField
var jvmField: Long = 2
