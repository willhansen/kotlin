// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: Enable when JS backend supports Java class library, since FunctionX are required for interoperation
// IGNORE_BACKEND: JS
class Works() : Function0<Any> {
    public override fun invoke():Any {
      return "Works" as Any
    }
}
class Broken() : Function0<String> {
    public override fun invoke():String {
      return "Broken"
    }
}

fun box(): String {
  konst works1: ()->Any = Works();
  works1()

  konst broken1: ()->String = Broken();
  broken1()

  return "OK"
}
