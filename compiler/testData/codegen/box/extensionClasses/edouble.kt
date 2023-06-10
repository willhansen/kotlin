// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class EntityContext {
    var d = DoubleArray(16)
}

class EDouble(konst i: Int) {
    context(EntityContext)
    var konstue:   Double
        get() = d[i]
        set(konstue) { d[i] = konstue }
}

fun box(): String {
    konst entityContext = EntityContext()
    with(entityContext) {
        konst eDouble = EDouble(0)
        eDouble.konstue = .2
        return if (eDouble.konstue == .2) "OK" else "fail"
    }
}
