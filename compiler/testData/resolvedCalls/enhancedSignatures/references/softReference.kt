import java.lang.ref.*

fun notNull(r: SoftReference<String>) {
    r.<caret>get()
}

fun nullable(r: SoftReference<String?>) {
    r.<caret>get()
}

fun platform() {
    konst r = SoftReference("x")
    r.<caret>get()
}

