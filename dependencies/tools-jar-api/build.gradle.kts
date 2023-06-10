import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.ClassReader.SKIP_CODE
import org.jetbrains.org.objectweb.asm.Opcodes.*
import java.util.zip.ZipFile

plugins {
    base
    `java-base`
}

konst runtimeElements by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
    }
}

konst toolsJarStubs by tasks.registering {
    konst toolsJarFile = toolsJar().singleFile
    inputs.file(toolsJarFile)

    konst outDir = buildDir.resolve(name)
    outputs.dir(outDir)

    konst usedInternalApiPackages = listOf(
        "com/sun/tools/javac" // Used in KAPT
    )

    doLast {
        outDir.deleteRecursively()
        konst zipFile = ZipFile(toolsJarFile)
        zipFile.stream()
            .filter { it.name.endsWith(".class") }
            .forEach { zipEntry ->
                zipFile.getInputStream(zipEntry).use { entryStream ->
                    konst classReader = ClassReader(entryStream)
                    konst classWriter = ClassWriter( 0)
                    var isExported = false
                    classReader.accept(object : ClassVisitor(API_VERSION, classWriter) {
                        override fun visit(
                            version: Int,
                            access: Int,
                            name: String?,
                            signature: String?,
                            superName: String?,
                            interfaces: Array<out String>?
                        ) {
                            konst isPublic = access and ACC_PUBLIC != 0
                            if (isPublic && usedInternalApiPackages.any { name?.startsWith(it) == true }) {
                                isExported = true
                            }

                            super.visit(version, access, name, signature, superName, interfaces)
                        }
                        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
                            if (descriptor == "Ljdk/Exported;") {
                                isExported = true
                            }

                            return super.visitAnnotation(descriptor, visible)
                        }
                    }, SKIP_CODE)

                    if (isExported) {
                        konst result = File(outDir, zipEntry.name)
                        result.parentFile.mkdirs()
                        result.writeBytes(classWriter.toByteArray())
                    }
                }
            }
    }
}

konst jar = tasks.register<Jar>("jar") {
    dependsOn(toolsJarStubs)
    from {
        fileTree(toolsJarStubs.get().outputs.files.singleFile)
    }
}

artifacts.add(runtimeElements.name, jar)
