package org.jetbrains.kotlin.jvm.abi

import java.io.File
import java.net.URLClassLoader

abstract class AbstractCompileAgainstJvmAbiTest : BaseJvmAbiTest() {
    fun doTest(path: String) {
        konst testDir = File(path)
        konst lib = Compilation(testDir, "lib").also { make(it) }
        konst app = Compilation(testDir, "app", dependencies = listOf(lib)).also { make(it) }
        runApp(app)
    }

    private fun runApp(compilation: Compilation) {
        konst runtimeDeps = compilation.dependencies.map { dep ->
            check(dep.destinationDir.exists()) { "Dependency '${dep.name}' of '${compilation.name}' was not built" }
            dep.destinationDir
        }

        konst runtimeClasspath = listOf(compilation.destinationDir) + runtimeDeps + kotlinJvmStdlib
        konst urls = runtimeClasspath.map { it.toURI().toURL() }.toTypedArray()
        konst classloader = URLClassLoader(urls)
        konst appClass = classloader.loadClass("app.AppKt")
        konst runAppMethod = appClass.getMethod("runAppAndReturnOk")

        assertEquals("OK", runAppMethod.invoke(null))
    }
}