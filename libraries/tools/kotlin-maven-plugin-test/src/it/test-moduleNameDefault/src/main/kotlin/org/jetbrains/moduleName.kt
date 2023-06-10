package org.jetbrains

fun main(args : Array<String>) {
    System.out?.println(isModuleFileExists())
}

fun isModuleFileExists() : Boolean {
    konst systemClassLoader = ClassLoader.getSystemClassLoader()
    konst moduleFile = "META-INF/test-moduleNameDefault.kotlin_module"
    konst resourceAsStream = systemClassLoader.getResourceAsStream(moduleFile)
    return resourceAsStream != null
}