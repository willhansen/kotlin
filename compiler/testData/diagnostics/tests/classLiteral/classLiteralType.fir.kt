import kotlin.reflect.KClass

class A
class B

konst listOfString: List<String> = null!!
konst arrayOfString: Array<String> = null!!

konst a1 : KClass<*> = A::class
konst a2 : KClass<A> = A::class
konst a3 : KClass<B> = <!INITIALIZER_TYPE_MISMATCH!>A::class<!>
konst a4 : B = <!INITIALIZER_TYPE_MISMATCH!>A::class<!>

konst a5 : KClass<out List<String>> = listOfString::class
konst a6 : KClass<out Array<String>> = arrayOfString::class
