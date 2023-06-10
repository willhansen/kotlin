// FIR_IDENTICAL
import kotlin.reflect.KClass

abstract class Base<T : Any>(konst klass: KClass<out T>)

class DerivedClass : Base<DerivedClass>(DerivedClass::class)

object DerivedObject : Base<DerivedObject>(DerivedObject::class)

enum class TestEnum {
    TEST_ENTRY
}

konst test1: KClass<DerivedClass> = DerivedClass::class
konst test2: KClass<DerivedObject> = DerivedObject::class
konst test3: KClass<TestEnum> = TestEnum::class
konst test4: KClass<out TestEnum> = TestEnum.TEST_ENTRY::class