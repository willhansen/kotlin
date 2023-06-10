// FIR_IDENTICAL
// !CHECK_TYPE

import java.io.File

fun test() {
    konst dir = File("dir")
    konst files = dir.listFiles()?.toList() ?: listOf() // error
    files checkType { _<List<File>>() }
}