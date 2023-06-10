class ClassA {
    konst leakedObject: SealedInterface get() = PrivateObject
}

private object PrivateObject : SealedInterface {
    override fun getNumber() = 1
}
