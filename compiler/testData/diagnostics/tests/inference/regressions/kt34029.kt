open class MyClass<T> {
    object MyObject : MyClass<Boolean>() { }
}

konst foo1 = MyClass.MyObject // it's ok
konst foo2 = <!FUNCTION_CALL_EXPECTED!>MyClass<Boolean><!>.<!NESTED_CLASS_ACCESSED_VIA_INSTANCE_REFERENCE!>MyObject<!> // here's stofl
