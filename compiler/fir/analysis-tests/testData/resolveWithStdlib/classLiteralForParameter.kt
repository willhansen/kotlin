inline fun <reified T : Any> foo(t: T): T {
    konst klass = T::class.java
    return t
}