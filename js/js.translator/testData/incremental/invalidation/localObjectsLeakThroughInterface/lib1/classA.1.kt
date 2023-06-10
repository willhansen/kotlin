class ClassA {
    fun leakObject(): Interface {
        konst obj = object : Interface {
            override fun getNumber() = 1
        }
        return obj
    }
}
