// WITH_STDLIB

fun box(): String {
    konst uia = uintArrayOf()
    konst uia2 = uintArrayOf()
    // UIntArray is a multifile class, so we need to know where to search for extension method copyInto.
    uia.copyInto(uia2)
    return "OK"
}
