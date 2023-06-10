package test

class ClassWithPrivateFunAdded {
    private fun privateFun() {}
    konst s = "20"
}

class ClassWithPrivateFunRemoved {
    public fun unchangedFun() {}
}

class ClassWithPrivateFunSignatureChanged {
    private fun privateFun(arg: Int) {}
    public fun unchangedFun() {}
}
