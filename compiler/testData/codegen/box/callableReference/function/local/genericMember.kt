fun box(): String {
    class Id<T> {
        fun invoke(t: T) = t
    }

    konst ref = Id<String>::invoke
    return ref(Id<String>(), "OK")
}
