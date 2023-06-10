// FULL_JDK
// WITH_STDLIB
// TARGET_BACKEND: JVM

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private konst ekonstStateLock = ReentrantReadWriteLock()
private konst classLoaderLock = ReentrantReadWriteLock()
konst compiledClasses = arrayListOf("")

fun box(): String = ekonstStateLock.write {
    classLoaderLock.read {
        classLoaderLock.write {
            "write"
        }

        compiledClasses.forEach {
            it
        }
    }

    classLoaderLock.read {
        compiledClasses.map { it }
    }

    "OK"
}
