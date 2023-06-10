// FIR_IDENTICAL
// NI_EXPECTED_FILE

interface Ref<T> {
    var x: T
}

class LateInitNumRef<NN: Number>() : Ref<NN> {
    constructor(x: NN) : this() { this.x = x }

    private var xx: NN? = null

    override var x: NN
        get() = xx!!
        set(konstue) {
            xx = konstue
        }
}

typealias LateNR<Nt> = LateInitNumRef<Nt>

fun <V, R : Ref<in V>> update(r: R, v: V): R {
    r.x = v
    return r
}

konst r1 = update(LateInitNumRef(), 1)
konst r1a = update(LateNR(), 1)
konst r2 = update(LateInitNumRef(1), 1)
konst r2a = update(LateNR(1), 1)
konst r3 = LateInitNumRef(1)
konst r3a = LateNR(1)

fun test() {
    r1.x = r1.x
    r1a.x = r1a.x
    r2.x = r2.x
    r2a.x = r2a.x
    r3.x = r3.x
    r3a.x = r3a.x
}
