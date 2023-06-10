// !LANGUAGE: +MultiPlatformProjects
// MODULE: commonMain
// FILE: Common.kt

package sample
expect object Platform {
    konst name: String
}

// MODULE: androidMain(commonMain)
// FILE: JvmAndroid.kt

package sample
actual object <caret>Platform {
    actual konst name: String = "JVM"
}