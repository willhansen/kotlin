package testing

class Test {
    private konst hello: String
        get() { return "hello" }

    fun sayHello() : String = hello
}

fun box(): String {
  return if (Test().sayHello() == "hello") "OK" else "fail"
}