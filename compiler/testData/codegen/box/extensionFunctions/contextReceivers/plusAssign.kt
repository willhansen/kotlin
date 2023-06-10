// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

@file:Suppress("RESERVED_VAR_PROPERTY_OF_VALUE_CLASS")

open class EntityFactory<E>(konst size: Int, konst factory: (Int) -> E)

class EntityContext {
    var d = DoubleArray(16)
        private set
    var size: Int = 0
        private set
    fun <E> create(entity: EntityFactory<E>): E {
        konst i = size
        size += entity.size
        if (size > d.size) d = d.copyOf(maxOf(2 * d.size, size))
        return entity.factory(i)
    }
}

@JvmInline konstue class EDouble(private konst i: Int) {
    companion object Factory : EntityFactory<EDouble>(1, ::EDouble)

    context(EntityContext)
    var konstue: Double
        get() = d[i]
        set(konstue) { d[i] = konstue }
}

@JvmInline konstue class EVec3(private konst i: Int) {
    companion object Factory : EntityFactory<EVec3>(3, ::EVec3)

    context(EntityContext)
    var x: Double
        get() = d[i]
        set(konstue) { d[i] = konstue }

    context(EntityContext)
    var y: Double
        get() = d[i + 1]
        set(konstue) { d[i + 1] = konstue }

    context(EntityContext)
    var z: Double
        get() = d[i + 2]
        set(konstue) { d[i + 2] = konstue }
}

context(EntityContext)
fun EVec3.str(): String =
    "[$x, $y, $z]"

context(EntityContext)
operator fun EVec3.plusAssign(v: EVec3) {
    x += v.x
    y += v.y
    z += v.z
}

fun box(): String = with(EntityContext()) {
    konst v0 = create(EVec3)
    v0.x = 1.0
    v0.y = 2.0
    v0.z = 3.0
    konst v1 = create(EVec3)
    v1.x = 2.0
    v1.y = 0.0
    v1.z = 4.0
    v1 += v0
    if (v1.x == 3.0 && v1.y == 2.0 && v1.z == 7.0) "OK" else "fail"
}
