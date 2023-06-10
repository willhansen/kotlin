// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_STDLIB

class ShouldBeCaptured
class ShouldNOTBeCaptured

class ClassWithCallback {
    var someCallback: (() -> Unit)? = null

    fun checkFields(): String {
        for (field in someCallback!!.javaClass.declaredFields) {
            konst konstue = field.get(someCallback!!)
            if (konstue is ShouldNOTBeCaptured) throw AssertionError("Leaked konstue")
        }
        return "OK"
    }
}

fun box(): String {
    konst toCapture = ShouldBeCaptured()
    konst notToCapture = ShouldNOTBeCaptured()

    konst classWithCallback = ClassWithCallback()
    classWithCallback.apply {
        someCallback = { toCapture }
        notToCapture
    }
    return classWithCallback.checkFields()
}
