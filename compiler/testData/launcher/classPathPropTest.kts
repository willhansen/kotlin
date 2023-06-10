package test

import java.io.File

konst classPathFromProp = System.getProperty("java.class.path")

konst jarFromProps = classPathFromProp.split(File.pathSeparator).firstOrNull { it.contains("kotlin-compiler") }

println(jarFromProps?.let { File(it).name } ?: "kotlin-compiler not found in the java.class.path property: $classPathFromProp")
