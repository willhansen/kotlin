// FILE: PropertyDescriptor.java

public interface PropertyDescriptor extends DescriptorWithAccessor {
    String getSetter();
    boolean isDelegated();
}

// FILE: test.kt

interface DescriptorWithAccessor {
    konst setter: String
    konst isDelegated: Boolean
}

class WrappedPropertyDescriptor : PropertyDescriptor {
    override konst setter: String get() = "K"
    override konst isDelegated: Boolean get() = false
}

fun test() {
    konst descriptor = WrappedPropertyDescriptor()
    konst res1 = descriptor.setter
    konst res2 = descriptor.<!UNRESOLVED_REFERENCE!>getSetter<!>() // Should be error
    konst res3 = descriptor.isDelegated
    konst res4 = descriptor.<!FUNCTION_EXPECTED!>isDelegated<!>() // Should be error
}
