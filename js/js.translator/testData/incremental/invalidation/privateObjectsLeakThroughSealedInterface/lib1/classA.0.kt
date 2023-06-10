class ClassA {
    konst leakedObject: SealedInterface get() = PrivateObject
}

private object PrivateObject : SealedInterface
