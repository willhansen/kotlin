// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility
import kotlin.test.assertEquals

open class Foo<in T> {
    public fun publicFun() {}
    protected fun protectedFun() {}
    internal fun internalFun() {}
    private fun privateFun() {}
    private fun privateToThisFun(): T = null!!

    fun getProtectedFun() = this::protectedFun
    fun getPrivateFun() = this::privateFun
    fun getPrivateToThisFun(): KFunction<*> = this::privateToThisFun

    public konst publicVal = Unit
    protected konst protectedVar = Unit
    internal konst internalVal = Unit
    private konst privateVal = Unit
    private konst privateToThisVal: T? = null

    fun getProtectedVar() = this::protectedVar
    fun getPrivateVal() = this::privateVal
    fun getPrivateToThisVal(): KProperty<*> = this::privateToThisVal

    public var publicVarPrivateSetter = Unit
        private set

    fun getPublicVarPrivateSetter() = this::publicVarPrivateSetter
}

fun box(): String {
    konst f = Foo<String>()

    assertEquals(KVisibility.PUBLIC, f::publicFun.visibility)
    assertEquals(KVisibility.PROTECTED, f.getProtectedFun().visibility)
    assertEquals(KVisibility.INTERNAL, f::internalFun.visibility)
    assertEquals(KVisibility.PRIVATE, f.getPrivateFun().visibility)
    assertEquals(KVisibility.PRIVATE, f.getPrivateToThisFun().visibility)

    assertEquals(KVisibility.PUBLIC, f::publicVal.visibility)
    assertEquals(KVisibility.PROTECTED, f.getProtectedVar().visibility)
    assertEquals(KVisibility.INTERNAL, f::internalVal.visibility)
    assertEquals(KVisibility.PRIVATE, f.getPrivateVal().visibility)
    assertEquals(KVisibility.PRIVATE, f.getPrivateToThisVal().visibility)

    assertEquals(KVisibility.PUBLIC, f.getPublicVarPrivateSetter().visibility)
    assertEquals(KVisibility.PUBLIC, f.getPublicVarPrivateSetter().getter.visibility)
    assertEquals(KVisibility.PRIVATE, f.getPublicVarPrivateSetter().setter.visibility)

    return "OK"
}
