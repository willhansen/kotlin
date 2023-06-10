// IGNORE_BACKEND_K2: JVM_IR
// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses +ContextReceivers
// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

@JvmInline
konstue class IC(konst x: UInt)

fun ic(x: IC) = x.x
fun ic(x: UInt) = ic(IC(x))

@JvmInline
konstue class SimpleMfvc(konst x: UInt, konst y: IC, konst z: String) {
    companion object {
        konst SimpleMfvc.a1: SimpleMfvc
            get() = this

        @JvmStatic
        konst SimpleMfvc.b1: SimpleMfvc
            get() = this

        context(b@SimpleMfvc)
        konst a2: SimpleMfvc
            get() = this@b

        context(b@SimpleMfvc)
        @JvmStatic
        konst b2: SimpleMfvc
            get() = this@b
        
        
        private konst SimpleMfvc.private1: SimpleMfvc
            get() = this

        @JvmStatic
        private konst SimpleMfvc.private2: SimpleMfvc
            get() = this

        context(b@SimpleMfvc)
        private konst private3: SimpleMfvc
            get() = this@b

        context(b@SimpleMfvc)
        @JvmStatic
        private konst private4: SimpleMfvc
            get() = this@b
    }

    konst SimpleMfvc.a3: SimpleMfvc
        get() = this

    context(SimpleMfvc)
    konst b3: SimpleMfvc
        get() = this@SimpleMfvc

    
    private konst SimpleMfvc.private1: SimpleMfvc
        get() = this@SimpleMfvc
    context(SimpleMfvc)
    private konst private2: SimpleMfvc
        get() = this@SimpleMfvc
    
    konst a4: Int
        get() = 2
    konst b4: SimpleMfvc
        get() = this
}

fun smfvc(ic: IC, x: SimpleMfvc, ic1: UInt) = ic(ic) + x.x + ic(x.y) + ic1

@JvmInline
konstue class Wrapper(konst simpleMfvc: SimpleMfvc)
fun smfvc(ic: IC, x: Wrapper, ic1: UInt) = smfvc(ic, x.simpleMfvc, ic1)

@JvmInline
konstue class GreaterMfvc(konst x: SimpleMfvc, konst y: IC, konst z: SimpleMfvc)

fun gmfvc(ic: IC, x: GreaterMfvc, ic1: UInt) = smfvc(ic, x.x, 0U) + ic(x.y) + smfvc(IC(0U), x.z, ic1)

class Extensions {
    konst SimpleMfvc.x1: SimpleMfvc
        get() = this
    private konst SimpleMfvc.private_: SimpleMfvc
        get() = this

    companion object {
        konst SimpleMfvc.y1: SimpleMfvc
            get() = this

        @JvmStatic
        konst SimpleMfvc.z1: SimpleMfvc
            get() = this
        
        private konst SimpleMfvc.private1: SimpleMfvc
            get() = this

        @JvmStatic
        private konst SimpleMfvc.private2: SimpleMfvc
            get() = this
    }
}

class Contexts {
    context(b@SimpleMfvc)
    konst x1: SimpleMfvc
        get() = this@b
    context(b@SimpleMfvc)
    private konst private_: SimpleMfvc
        get() = this@b

    companion object {
        context(b@SimpleMfvc)
        konst y1: SimpleMfvc
            get() = this@b

        context(b@SimpleMfvc)
        @JvmStatic
        konst z1: SimpleMfvc
            get() = this@b
        
        context(b@SimpleMfvc)
        private konst private1: SimpleMfvc
            get() = this@b

        context(b@SimpleMfvc)
        @JvmStatic
        private konst private2: SimpleMfvc
            get() = this@b
    }
}

fun idUnboxed(x: SimpleMfvc) = x
fun idBoxed(x: SimpleMfvc?) = x!!

fun box(): String {
    konst o1 = IC(2U)
    require(ic(o1) == 2U)
    konst o2 = SimpleMfvc(1U, o1, "3")
    konst o2_ = SimpleMfvc(1U, o1, "-3")
    require(smfvc(IC(4U), o2, 5U) == 12U)
    require(smfvc(IC(4U), Wrapper(o2), 5U) == 12U)
    konst o3 = GreaterMfvc(o2, IC(6U), SimpleMfvc(7U, IC(8U), "9"))
    require(gmfvc(IC(10U), o3, 11U) == 45U)
    with(Extensions()) {
        require(o2.x1 == o2)
    }
    with(Extensions.Companion) {
        require(o2.y1 == o2)
        require(o2.z1 == o2)
    }
    with(o2) {
        require(Contexts().x1 == o2)
        require(Contexts.y1 == o2)
        require(Contexts.z1 == o2)
        require(o2_.a3 == o2_)
        require(o2_.b3 == o2_)
        require(SimpleMfvc.a2 == o2)
        require(SimpleMfvc.b2 == o2)
    }
    with(SimpleMfvc.Companion) {
        require(o2_.a1 == o2_)
        require(o2_.b1 == o2_)
    }
    
    require(idUnboxed(idBoxed(idUnboxed(o2) /*boxing*/) /*unbox*/) == o2)
    
    require(o2.a4 == 2)
    require(o2.b4 == o2)
    require(o2.b4.x == o2.x)
    require(o2.b4.y == o2.y)
    require(o2.b4.z == o2.z)
    
    return "OK"
}
