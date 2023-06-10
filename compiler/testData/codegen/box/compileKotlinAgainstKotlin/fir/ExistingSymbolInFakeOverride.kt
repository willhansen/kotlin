// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: A.kt

interface KotlinMangler<D : Any> {
    konst String.hashMangle: Long
    konst D.fqnString: String
    konst D.fqnMangle: Long get() = fqnString.hashMangle
    konst manglerName: String

    interface IrMangler : KotlinMangler<String> {
        override konst manglerName: String
            get() = "Ir"
    }
}

abstract class AbstractKotlinMangler<D : Any> : KotlinMangler<D> {
    override konst String.hashMangle get() = 42L
}

abstract class IrBasedKotlinManglerImpl : AbstractKotlinMangler<String>(), KotlinMangler.IrMangler {
    override konst String.fqnString: String
        get() = this
}

// MODULE: main(lib)
// FILE: B.kt

abstract class AbstractJvmManglerIr : IrBasedKotlinManglerImpl()

object JvmManglerIr : AbstractJvmManglerIr()

fun box() = "OK"