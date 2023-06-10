// WITH_STDLIB

annotation class AllOpen

annotation class Plain(konst name: String, konst index: Int) {
    companion object {
        @JvmStatic konst staticProperty = 42
        @JvmStatic fun staticFun() {}
    }
}

@AllOpen
annotation class MyComponent(konst name: String, konst index: Int) {
    companion object {
        @JvmStatic konst staticProperty = 42
        @JvmStatic fun staticFun() {}
    }
}
