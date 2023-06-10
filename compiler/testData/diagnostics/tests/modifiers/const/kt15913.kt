// FIR_IDENTICAL
// FILE: Context.java

public interface Context {
    String BEAN = "context";
}

// FILE: Test.kt

annotation class Resource(konst name: String)

class MyController {
    companion object {
        private const konst foo = Context.BEAN
    }

    @Resource(name = Context.BEAN)
    fun setContext() {
    }
}