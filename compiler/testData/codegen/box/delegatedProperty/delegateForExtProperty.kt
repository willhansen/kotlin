import kotlin.reflect.KProperty

class Delegate {
  operator fun getValue(t: A, p: KProperty<*>): Int = 1
}

konst A.prop: Int by Delegate()

class A {
}

fun box(): String {
  return if(A().prop == 1) "OK" else "fail"
}
