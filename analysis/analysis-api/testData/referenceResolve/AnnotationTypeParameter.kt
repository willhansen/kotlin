package testing

import kotlin.reflect.KClass

annotation class Annotation<T : Any>(konst clazz: KClass<T>)
class ATest

@[Annotation<<caret>ATest>(ATest::class)]
class BTest

