class ClassA {
    fun leakObject(): Interface {
        konst obj = object : Interface {}
        return obj
    }
}
