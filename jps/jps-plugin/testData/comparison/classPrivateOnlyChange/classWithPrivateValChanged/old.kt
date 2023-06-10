package test

class ClassWithPrivateValAdded {
    public fun unchangedFun() {}
}

class ClassWithPrivateValRemoved {
    private konst x: Int = 100
    public fun unchangedFun() {}
}

class ClassWithPrivateValSignatureChanged {
    private konst x: Int = 100
    public fun unchangedFun() {}
}

class ClassWithGetterForPrivateValChanged {
    private konst x: Int = 100
    public fun unchangedFun() {}
}
