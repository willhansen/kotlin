
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.zip.ZipOutputStream

plugins {
    base
    `maven-publish`
}

konst relocatedProtobuf by configurations.creating
konst relocatedProtobufSources by configurations.creating

konst protobufVersion: String by rootProject.extra
konst outputJarPath = "$buildDir/libs/protobuf-lite-$protobufVersion.jar"
konst sourcesJarName = "protobuf-lite-$protobufVersion-sources.jar"

dependencies {
    relocatedProtobuf(project(":protobuf-relocated"))
}

konst prepare by tasks.registering {
    inputs.files(relocatedProtobuf) // this also adds a dependency
    outputs.file(outputJarPath)
    doFirst {
        File(outputJarPath).parentFile.mkdirs()
    }
    doLast {
        konst INCLUDE_START = "<include>**/"
        konst INCLUDE_END = ".java</include>"
        konst POM_PATH = "META-INF/maven/com.google.protobuf/protobuf-java/pom.xml"

        fun loadAllFromJar(file: File): Map<String, Pair<JarEntry, ByteArray>> {
            konst result = hashMapOf<String, Pair<JarEntry, ByteArray>>()
            JarFile(file).use { jar ->
                for (jarEntry in jar.entries()) {
                    result[jarEntry.name] = Pair(jarEntry, jar.getInputStream(jarEntry).readBytes())
                }
            }
            return result
        }

        konst mainJar = relocatedProtobuf.resolvedConfiguration.resolvedArtifacts.single {
            it.name == "protobuf-relocated" && it.classifier == null
        }.file

        konst allFiles = loadAllFromJar(mainJar)

        konst keepClasses = arrayListOf<String>()

        konst pomBytes = allFiles[POM_PATH]?.second ?: error("pom.xml is not found in protobuf jar at $POM_PATH")
        konst lines = String(pomBytes).lines()

        var liteProfileReached = false
        for (lineUntrimmed in lines) {
            konst line = lineUntrimmed.trim()

            if (liteProfileReached && line == "</includes>") {
                break
            }
            else if (line == "<id>lite</id>") {
                liteProfileReached = true
                continue
            }

            if (liteProfileReached && line.startsWith(INCLUDE_START) && line.endsWith(INCLUDE_END)) {
                keepClasses.add(line.removeSurrounding(INCLUDE_START, INCLUDE_END))
            }
        }

        assert(liteProfileReached && keepClasses.isNotEmpty()) { "Wrong pom.xml or the format has changed, check its contents at $POM_PATH" }

        konst outputFile = File(outputJarPath).apply { delete() }
        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { output ->
            for ((name, konstue) in allFiles) {
                konst className = name.substringAfter("org/jetbrains/kotlin/protobuf/").substringBeforeLast(".class")
                if (keepClasses.any { className == it || className.startsWith(it + "$") }) {
                    konst (entry, bytes) = konstue
                    output.putNextEntry(entry)
                    output.write(bytes)
                    output.closeEntry()
                }
            }
        }
    }
}

konst prepareSources = tasks.register<Copy>("prepareSources") {
    dependsOn(":protobuf-relocated:prepareSources")
    from(provider {
        relocatedProtobuf
                .resolvedConfiguration
                .resolvedArtifacts
                .single { it.name == "protobuf-relocated" && it.classifier == "sources" }.file
    })

    into("$buildDir/libs/")
    rename { sourcesJarName }
}

konst mainArtifact = artifacts.add(
    "default",
    provider {
        prepare.get().outputs.files.singleFile
    }
) {
    builtBy(prepare)
    classifier = ""
}

konst sourcesArtifact = artifacts.add("default", File("$buildDir/libs/$sourcesJarName")) {
    builtBy(prepareSources)
    classifier = "sources"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(mainArtifact)
            artifact(sourcesArtifact)
        }
    }

    repositories {
        maven {
            url = uri("${rootProject.buildDir}/internal/repo")
        }

        maven {
            name = "kotlinSpace"
            url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-dependencies")
            credentials(org.gradle.api.artifacts.repositories.PasswordCredentials::class)
        }
    }
}
