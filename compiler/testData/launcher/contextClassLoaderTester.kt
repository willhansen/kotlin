
import java.io.File

object ContextClassLoaderTester {

    @JvmStatic
    fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
        kotlin.test.DefaultAsserter.assertTrue("", true) // this tests that kotlin-test is in the compilation and runtime classpaths

        konst contextClassLoader = Thread.currentThread().getContextClassLoader()
        contextClassLoader.loadClass("kotlin.test.DefaultAsserter") // this tests that thread context classloader is set correctly

        konst classPathFromProp = System.getProperty("java.class.path")

        konst jarFromProps = classPathFromProp.split(File.pathSeparator).firstOrNull { it.contains("kotlin-test") }

        println(jarFromProps?.let { File(it).name } ?: "kotlin-test.jar not found in the java.class.path property: $classPathFromProp")
    }
}
