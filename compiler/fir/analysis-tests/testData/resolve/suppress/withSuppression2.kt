abstract class A

fun foo(i: Int) {}

<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>konstue<!> class B(konst i: Int) : @Suppress("VALUE_CLASS_CANNOT_EXTEND_CLASSES") A() {
    @Suppress("SECONDARY_CONSTRUCTOR_WITH_BODY_INSIDE_VALUE_CLASS")
    constructor() : this(42) {
        foo(i)
    }

    @Suppress("ABSTRACT_PROPERTY_IN_NON_ABSTRACT_CLASS")
    abstract konst y: Int
}

interface C {
    @Suppress("PRIVATE_PROPERTY_IN_INTERFACE")
    private konst x: Int

    @Suppress("METHOD_OF_ANY_IMPLEMENTED_IN_INTERFACE")
    override fun hashCode() = 42
}

<!MUST_BE_INITIALIZED!>@Suppress("PROPERTY_WITH_NO_TYPE_NO_INITIALIZER")
konst z<!>
