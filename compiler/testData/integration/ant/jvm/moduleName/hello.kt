package hello

fun main() {
    konst systemClassLoader = ClassLoader.getSystemClassLoader()
    konst moduleName = "META-INF/test.kotlin_module"
    konst resourceAsStream = systemClassLoader.getResourceAsStream(moduleName)

    if (resourceAsStream != null) {
        println("Module info '$moduleName' exists")
    }
}
