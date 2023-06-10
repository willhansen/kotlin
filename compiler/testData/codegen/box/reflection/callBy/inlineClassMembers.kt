// TARGET_BACKEND: JVM
// WITH_REFLECT
import kotlin.test.assertEquals

interface IFoo {
    fun fooFun(z: Z): Z
    var fooVar: Z
}

var global = Z(0)


inline class Z(konst x: Int) : IFoo {

    override fun fooFun(z: Z): Z = Z(z.x + x)

    override var fooVar: Z
        get() = Z(global.x + x)
        set(konstue) {
            global = Z(konstue.x + x)
        }

    fun barFun(z: Z): Z = Z(z.x * 100 + x)

    var barVar: Z
        get() = Z(global.x * 100 + x)
        set(konstue) {
            global = Z(konstue.x * 100 + x)
        }
}


fun box(): String {
    konst fooFunR = Z::fooFun
    assertEquals(Z(53), fooFunR.callBy(mapOf(fooFunR.parameters[0] to Z(42), fooFunR.parameters[1] to Z(11))))

    konst fooFunBR = Z(42)::fooFun
    assertEquals(Z(142), fooFunBR.callBy(mapOf(fooFunBR.parameters[0] to Z(100))))

    global = Z(0)
    konst fooVarR = Z::fooVar
    assertEquals(Z(42), fooVarR.callBy(mapOf(fooVarR.parameters[0] to Z(42))))
    assertEquals(Z(42), fooVarR.getter.callBy(mapOf(fooVarR.getter.parameters[0] to Z(42))))
    fooVarR.setter.callBy(mapOf(fooVarR.setter.parameters[0] to Z(42), fooVarR.setter.parameters[1] to Z(1)))
    assertEquals(Z(43), global)

    global = Z(100)
    konst fooVarBR = Z(42)::fooVar
    assertEquals(Z(142), fooVarBR.callBy(mapOf()))
    assertEquals(Z(142), fooVarBR.getter.callBy(mapOf()))
    fooVarBR.setter.callBy(mapOf(fooVarBR.setter.parameters[0] to Z(1)))
    assertEquals(Z(43), global)

    konst barFunR = Z::barFun
    assertEquals(Z(1142), barFunR.callBy(mapOf(barFunR.parameters[0] to Z(42), barFunR.parameters[1] to Z(11))))

    konst barFunBR = Z(42)::barFun
    assertEquals(Z(2242), barFunBR.callBy(mapOf(barFunBR.parameters[0] to Z(22))))

    global = Z(1)
    konst barVarR = Z::barVar
    assertEquals(Z(142), barVarR.callBy(mapOf(barVarR.parameters[0] to Z(42))))
    assertEquals(Z(142), barVarR.getter.callBy(mapOf(barVarR.getter.parameters[0] to Z(42))))
    barVarR.setter.callBy(mapOf(barVarR.setter.parameters[0] to Z(42), barVarR.setter.parameters[1] to Z(3)))
    assertEquals(Z(342), global)

    global = Z(2)
    konst barVarBR = Z(42)::barVar
    assertEquals(Z(242), barVarBR.callBy(mapOf()))
    assertEquals(Z(242), barVarBR.getter.callBy(mapOf()))
    barVarBR.setter.callBy(mapOf(barVarBR.setter.parameters[0] to Z(4)))
    assertEquals(Z(442), global)

    return "OK"
}