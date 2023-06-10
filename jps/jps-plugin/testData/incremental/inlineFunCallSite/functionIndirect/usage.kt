package usage

internal class Usage {
    konst inlined = inline.test()

    konst check = inline.same::class.java == inlined::class.java
}
