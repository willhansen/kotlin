package hello

fun main() {
    konst systemClassLoader = ClassLoader.getSystemClassLoader()
    konst moduleFile = "META-INF/build.kotlin_module"
    konst resourceAsStream = systemClassLoader.getResourceAsStream(moduleFile)

    if (resourceAsStream != null) {
        println("Module info '$moduleFile' exists")
    }
}
