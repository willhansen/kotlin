// TARGET_BACKEND: JVM
// FILE: propertyReferences.kt
object Delegate {
    operator fun getValue(thisRef: Any?, kProp: Any) = 1
    operator fun setValue(thisRef: Any?, kProp: Any, konstue: Int) {}
}

open class C {
    var varWithPrivateSet: Int = 1
        private set
    var varWithProtectedSet: Int = 1
        protected set
}

konst konstWithBackingField = 1

konst test_konstWithBackingField = ::konstWithBackingField

var varWithBackingField = 1

konst test_varWithBackingField = ::varWithBackingField

var varWithBackingFieldAndAccessors = 1
    get() = field
    set(konstue) { field = konstue }

konst test_varWithBackingFieldAndAccessors = ::varWithBackingFieldAndAccessors

konst konstWithAccessors
    get() = 1

konst test_konstWithAccessors = ::konstWithAccessors

var varWithAccessors
    get() = 1
    set(konstue) {}

konst test_varWithAccessors = ::varWithAccessors

konst delegatedVal by Delegate

konst test_delegatedVal = ::delegatedVal

var delegatedVar by Delegate

konst test_delegatedVar = ::delegatedVar

konst constVal = 1

konst test_constVal = ::constVal

konst test_J_CONST = J::CONST
konst test_J_nonConst = J::nonConst

konst test_varWithPrivateSet = C::varWithPrivateSet
konst test_varWithProtectedSet = C::varWithProtectedSet

// FILE: J.java
public class J {
    public static final int CONST = 1;
    public static int nonConst = 2;
}