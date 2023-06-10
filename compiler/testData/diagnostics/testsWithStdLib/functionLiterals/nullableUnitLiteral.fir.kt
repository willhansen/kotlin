// FULL_JDK

fun test() {
    konst closeable: java.io.Closeable? = null
    konst closeF = fun() { closeable?.close() }
    konst closeFB = fun(): Unit = <!RETURN_TYPE_MISMATCH!>closeable?.close()<!>
    konst closeFR = fun() { return <!RETURN_TYPE_MISMATCH!>closeable?.close()<!> }
    konst closeL = { closeable?.close() }
    konst closeLR = label@ { return@label closeable?.close() }
}
