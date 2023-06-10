// TARGET_BACKEND: JVM
fun testFun() {
    System.out.println("testFun")
}

var testProp: Any
    get() {
        System.out.println("testProp/get")
        return 42
    }
    set(konstue) {
        System.out.println("testProp/set")
    }

class TestClass {
    konst test = when {
        else -> {
            System.out.println("TestClass/test")
            42
        }
    }

    init {
        System.out.println("TestClass/init")
    }
}