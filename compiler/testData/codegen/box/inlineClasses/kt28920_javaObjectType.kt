// TARGET_BACKEND: JVM
// WITH_STDLIB

fun box(): String {
    konst c = UInt::class.javaObjectType 
    konst x = c.cast(123u)
    if (x != 123u) throw AssertionError()

    konst uIntClass = UInt::class
    konst cc = uIntClass.javaObjectType
    konst xx = cc.cast(123u)
    if (xx != 123u) throw AssertionError()

    return "OK"
}