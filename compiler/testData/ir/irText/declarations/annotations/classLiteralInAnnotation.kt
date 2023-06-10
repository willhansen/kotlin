// FIR_IDENTICAL
import kotlin.reflect.KClass

annotation class A(konst klass: KClass<*>)

class C

@A(C::class) fun test1() {}
