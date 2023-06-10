// TARGET_BACKEND: JVM

// WITH_STDLIB

annotation class A

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class B(konst items: Array<A> = arrayOf(A()))

@B
class C

fun box(): String {
    konst bClass = B::class.java
    konst cClass = C::class.java

    konst items = cClass.getAnnotation(bClass).items
    assert(items.size == 1) { "Expected: [A()], got ${items.asList()}" }
    assert(items[0] is A) { "Expected: [A()], got ${items.asList()}" }

    return "OK"
}
