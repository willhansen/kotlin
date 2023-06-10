// FIR_IDENTICAL
package test
import kotlin.reflect.KClass

annotation class AnnClass(konst a: KClass<*>)

class MyClass {

    @AnnClass(MyClass::class)
    companion object {
    }

}
