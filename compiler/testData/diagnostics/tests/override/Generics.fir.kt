package override.generics

interface MyTrait<T> {
    fun foo(t: T) : T
}

abstract class MyAbstractClass<T> {
    abstract fun bar(t: T) : T
    abstract konst pr : T
}

interface MyProps<T> {
    konst p : T
}

open class MyGenericClass<T>(t : T) : MyTrait<T>, MyAbstractClass<T>(), MyProps<T> {
    override fun foo(t: T) = t
    override fun bar(t: T) = t
    override konst p : T = t
    override konst pr : T = t
}

class MyChildClass() : MyGenericClass<Int>(1) {}
class MyChildClass1<T>(t : T) : MyGenericClass<T>(t) {}
class MyChildClass2<T>(t : T) : MyGenericClass<T>(t) {
    fun foo(t: T) = t
    konst pr : T = t
    override fun bar(t: T) = t
    override konst p : T = t
}

open class MyClass() : MyTrait<Int>, MyAbstractClass<String>() {
    override fun foo(t: Int) = t
    override fun bar(t: String) = t
    override konst pr : String = "1"
}

abstract class MyAbstractClass1 : MyTrait<Int>, MyAbstractClass<String>() {
    override fun foo(t: Int) = t
    override fun bar(t: String) = t
}

<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class MyIllegalGenericClass1<!><T> : MyTrait<T>, MyAbstractClass<T>() {}
<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class MyIllegalGenericClass2<!><T, R>(r : R) : MyTrait<T>, MyAbstractClass<R>() {
    <!NOTHING_TO_OVERRIDE!>override<!> fun foo(r: R) = r
    <!NOTHING_TO_OVERRIDE!>override<!> konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>T<!>> pr : R = r
}
<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class MyIllegalClass1<!> : MyTrait<Int>, MyAbstractClass<String>() {}
abstract class MyLegalAbstractClass1 : MyTrait<Int>, MyAbstractClass<String>() {}

<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class MyIllegalClass2<!><T>(t : T) : MyTrait<Int>, MyAbstractClass<Int>() {
    fun foo(t: T) = t
    fun bar(t: T) = t
    konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>R<!>> pr : T = t
}
abstract class MyLegalAbstractClass2<T>(t : T) : MyTrait<Int>, MyAbstractClass<Int>() {
    fun foo(t: T) = t
    fun bar(t: T) = t
    konst <<!TYPE_PARAMETER_OF_PROPERTY_NOT_USED_IN_RECEIVER!>R<!>> pr : T = t
}
