import kotlin.reflect.KClass

class SomeClass

inline fun <reified K> foo(klass: KClass<*>): K = null!!

konst some: Map<String, String> by lazy {
    SomeClass::class.let {
        foo(it)
    }
}
