import org.gradle.api.tasks.PathSensitivity.RELATIVE
import java.io.File

plugins {
    base
    `maven-publish`
}

konst builtinsSrc = fileFrom(rootDir, "core", "builtins", "src")
konst builtinsNative = fileFrom(rootDir, "core", "builtins", "native")
konst kotlinReflectCommon = fileFrom(rootDir, "libraries/stdlib/src/kotlin/reflect/")
konst kotlinReflectJvm = fileFrom(rootDir, "libraries/stdlib/jvm/src/kotlin/reflect")
konst kotlinRangesCommon = fileFrom(rootDir, "libraries/stdlib/src/kotlin/ranges")
konst kotlinCollectionsCommon = fileFrom(rootDir, "libraries/stdlib/src/kotlin/collections")
konst kotlinAnnotationsCommon = fileFrom(rootDir, "libraries/stdlib/src/kotlin/annotations")
konst builtinsCherryPicked = fileFrom(buildDir, "src/reflect")
konst rangesCherryPicked = fileFrom(buildDir, "src/ranges")
konst builtinsCherryPickedJvm = fileFrom(buildDir, "src-jvm/reflect")

konst runtimeElements by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
    }
}

konst runtimeElementsJvm by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(Attribute.of("builtins.platform", String::class.java), "JVM")
    }
}

konst prepareRangeSources by tasks.registering(Sync::class) {
    from(kotlinRangesCommon) {
        exclude("Ranges.kt")
    }
    from(kotlinCollectionsCommon) {
        include("PrimitiveIterators.kt")
    }
    from(kotlinAnnotationsCommon) {
        include("ExperimentalStdlibApi.kt")
        include("OptIn.kt")
        include("WasExperimental.kt")
    }

    into(rangesCherryPicked)
}

konst prepareSources by tasks.registering(Sync::class) {
    from(kotlinReflectCommon) {
        exclude("typeOf.kt")
        exclude("KClasses.kt")
    }
    into(builtinsCherryPicked)
}

konst prepareSourcesJvm by tasks.registering(Sync::class) {
    from(kotlinReflectJvm) {
        exclude("TypesJVM.kt")
        exclude("KClassesImpl.kt")
    }
    from(kotlinReflectCommon) {
        include("KTypeProjection.kt")
        include("KClassifier.kt")
        include("KTypeParameter.kt")
        include("KVariance.kt")
    }
    into(builtinsCherryPickedJvm)
}

fun serializeTask(name: String, sourcesTask: TaskProvider<*>, inDirs: List<File>) =
    tasks.register(name, NoDebugJavaExec::class) {
        dependsOn(sourcesTask, prepareRangeSources)
        konst outDir = buildDir.resolve(this.name)
        inDirs.forEach { inputs.dir(it).withPathSensitivity(RELATIVE) }
        outputs.dir(outDir)
        outputs.cacheIf { true }

        classpath(rootProject.buildscript.configurations["bootstrapCompilerClasspath"])
        mainClass.set("org.jetbrains.kotlin.serialization.builtins.RunKt")
        jvmArgs(listOfNotNull(
            "-Didea.io.use.nio2=true",
            "-Dkotlin.builtins.serializer.log=true".takeIf { logger.isInfoEnabled }
        ))
        args(
            pathRelativeToWorkingDir(outDir),
            *inDirs.map(::pathRelativeToWorkingDir).toTypedArray()
        )
    }

konst serialize = serializeTask("serialize", prepareSources, listOf(builtinsSrc, builtinsNative, builtinsCherryPicked, rangesCherryPicked))

konst serializeJvm = serializeTask("serializeJvm", prepareSourcesJvm, listOf(builtinsSrc, builtinsNative, builtinsCherryPickedJvm, rangesCherryPicked))

konst builtinsJar by task<Jar> {
    dependsOn(serialize)
    from(serialize) { include("kotlin/**") }
    destinationDirectory.set(File(buildDir, "libs"))
}

konst builtinsJvmJar by task<Jar> {
    dependsOn(serializeJvm)
    from(serializeJvm) { include("kotlin/**") }
    archiveClassifier.set("jvm")
    destinationDirectory.set(File(buildDir, "libs"))
}

konst assemble by tasks.getting {
    dependsOn(serialize)
    dependsOn(serializeJvm)
}

konst builtinsJarArtifact = artifacts.add(runtimeElements.name, builtinsJar)
artifacts.add(runtimeElementsJvm.name, builtinsJvmJar)

publishing {
    publications {
        create<MavenPublication>("internal") {
            artifact(builtinsJarArtifact)
        }
    }

    repositories {
        maven("${rootProject.buildDir}/internal/repo")
    }
}
