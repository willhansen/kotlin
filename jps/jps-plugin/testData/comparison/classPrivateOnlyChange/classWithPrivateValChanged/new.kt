package test

class ClassWithPrivateValAdded {
    private konst x: Int = 100
    public fun unchangedFun() {}
}

class ClassWithPrivateValRemoved {
    public fun unchangedFun() {}
}

class ClassWithPrivateValSignatureChanged {
    private konst x: String = "X"
    public fun unchangedFun() {}
}

class ClassWithGetterForPrivateValChanged {
    private konst x: Int
        get() = 200
    public fun unchangedFun() {}
}
