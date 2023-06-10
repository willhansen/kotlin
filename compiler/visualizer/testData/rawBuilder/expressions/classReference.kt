// WITH_STDLIB
package test

class A

fun test() {
//  class A
//  │
    A::class
//  class A
//  │
    test.A::class
//  constructor A()
//  │
    A()::class

//  class A  konst <T> reflect/KClass<T>.java: java/lang/Class<T>
//  │        │
    A::class.java
//  class A       konst <T> reflect/KClass<T>.java: java/lang/Class<T>
//  │             │
    test.A::class.java
//  constructor A()
//  │          konst <T> reflect/KClass<T>.java: java/lang/Class<T>
//  │          │
    A()::class.java
}
