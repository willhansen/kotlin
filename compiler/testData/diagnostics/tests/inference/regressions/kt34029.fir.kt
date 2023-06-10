open class MyClass<T> {
    object MyObject : MyClass<Boolean>() { }
}

konst foo1 = MyClass.MyObject // it's ok
konst foo2 = MyClass<Boolean>.MyObject // here's stofl
