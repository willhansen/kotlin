package testing

annotation class Annotation<T>(konst clazz: Class<T>)
class ATest

class A<T>

@[Annotation<ATest>(A<<caret>ATest>())]
class BTest

