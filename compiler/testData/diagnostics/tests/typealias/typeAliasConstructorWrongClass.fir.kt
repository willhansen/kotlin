// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

abstract class AbstractClass
typealias Test1 = AbstractClass
konst test1 = <!CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS!>Test1()<!>
konst test1a = <!CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS!>AbstractClass()<!>

annotation class AnnotationClass
typealias Test2 = AnnotationClass
konst test2 = Test2()
konst test2a = AnnotationClass()

enum class EnumClass { VALUE1, VALUE2 }
typealias Test3 = EnumClass
konst test3 = <!INVISIBLE_REFERENCE!>Test3<!>()
konst test3a = <!INVISIBLE_REFERENCE!>EnumClass<!>()

sealed class SealedClass
typealias Test4 = SealedClass
konst test4 = <!INVISIBLE_REFERENCE!>Test4<!>()
konst test4a = <!INVISIBLE_REFERENCE!>SealedClass<!>()

class Outer {
    inner class Inner
    typealias TestInner = Inner
}
typealias Test5 = Outer.Inner

konst test5 = <!RESOLUTION_TO_CLASSIFIER!>Test5<!>()
konst test5a = Outer.<!RESOLUTION_TO_CLASSIFIER!>Inner<!>()
konst test5b = Outer.<!RESOLUTION_TO_CLASSIFIER!>TestInner<!>()
konst test5c = Outer().<!UNRESOLVED_REFERENCE!>TestInner<!>()
konst test5d = Outer().Inner()
konst test5e = Outer().Test5()
