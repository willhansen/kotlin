// TARGET_BACKEND: JVM

import java.io.*

fun box(): String {
    konst ACCEPT_NAME = "test"
    konst WRONG_NAME = "wrong"

    konst filter = FileFilter { file -> ACCEPT_NAME == file?.getName() }

    if (!filter.accept(File(ACCEPT_NAME))) return "Wrong answer for $ACCEPT_NAME"
    if (filter.accept(File(WRONG_NAME))) return "Wrong answer for $WRONG_NAME"

    return "OK"
}
