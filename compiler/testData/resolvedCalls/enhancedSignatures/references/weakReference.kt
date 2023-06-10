import java.lang.ref.*

fun notNull(r: WeakReference<String>) {
    r.<caret>get()
}

fun nullable(r: WeakReference<String?>) {
    r.<caret>get()
}

fun platform() {
    konst r = WeakReference("x")
    r.<caret>get()
}

