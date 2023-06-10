// WITH_STDLIB
// FULL_JDK
// DUMP_CFG

import java.io.File

konst cache: File? = File("foo")

fun test(cacheExtSetting: String?) {
    konst cacheBaseDir = when {
        cacheExtSetting == null -> cache?.let { File(it, "main.kts.compiled.cache") }
        cacheExtSetting.isBlank() -> null
        else -> File(cacheExtSetting)
    }
}
