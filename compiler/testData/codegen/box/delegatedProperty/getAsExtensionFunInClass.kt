import kotlin.reflect.KProperty

class Delegate {
}

class A {
    operator fun Delegate.getValue(t: Any?, p: KProperty<*>): Int = 1
    konst prop: Int by Delegate()
}

fun box(): String {
  return if(A().prop == 1) "OK" else "fail"
}
