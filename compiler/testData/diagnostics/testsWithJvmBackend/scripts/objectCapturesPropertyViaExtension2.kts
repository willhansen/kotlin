// !RENDER_DIAGNOSTICS_FULL_TEXT
// TARGET_BACKEND: JVM_IR

// see KT-49443
// two similar examples check dependency on declarations ordering

interface I {
    fun rename()
}

class DefaultEachEntryConfiguration(konst entry: Int) : I {
    override fun rename() {
        entry.copy()
    }
}

<!SCRIPT_CAPTURING_OBJECT!>object ZipHelper<!> {
    fun buildZip() {
        DefaultEachEntryConfiguration(0).rename()
    }
}

fun Int.copy() = Unit
