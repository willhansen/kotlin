@file:Suppress("unused") // usages in build scripts are not tracked properly

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencySubstitution
import org.gradle.api.artifacts.component.ProjectComponentSelector
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.register
import java.io.File

const konst kotlinEmbeddableRootPackage = "org.jetbrains.kotlin"

konst packagesToRelocate =
    listOf(
        "com.intellij",
        "com.google",
        "com.sampullara",
        "org.apache",
        "org.jdom",
        "org.picocontainer",
        "org.jline",
        "org.fusesource",
        "net.jpountz",
        "one.util.streamex",
        "it.unimi.dsi.fastutil",
        "kotlinx.collections.immutable"
    )

// The shaded compiler "dummy" is used to rewrite dependencies in projects that are used with the embeddable compiler
// on the runtime and use some shaded dependencies from the compiler
// To speed-up rewriting process we want to have this dummy as small as possible.
// But due to the shadow plugin bug (https://github.com/johnrengelman/shadow/issues/262) it is not possible to use
// packagesToRelocate list to for the include list. Therefore the exclude list has to be created.
konst packagesToExcludeFromDummy =
    listOf(
        "org/jetbrains/kotlin/**",
        "org/intellij/lang/annotations/**",
        "org/jetbrains/jps/**",
        "META-INF/**",
        "com/sun/jna/**",
        "com/thoughtworks/xstream/**",
        "javaslang/**",
        "*.proto",
        "messages/**",
        "net/sf/cglib/**",
        "one/util/streamex/**",
        "org/iq80/snappy/**",
        "org/jline/**",
        "org/xmlpull/**",
        "*.txt"
    )

private fun ShadowJar.configureEmbeddableCompilerRelocation(withJavaxInject: Boolean = true) {
    relocate("com.google.protobuf", "org.jetbrains.kotlin.protobuf")
    packagesToRelocate.forEach {
        relocate(it, "$kotlinEmbeddableRootPackage.$it")
    }
    if (withJavaxInject) {
        relocate("javax.inject", "$kotlinEmbeddableRootPackage.javax.inject")
    }
    relocate("org.fusesource", "$kotlinEmbeddableRootPackage.org.fusesource") {
        // TODO: remove "it." after #KT-12848 get addressed
        exclude("org.fusesource.jansi.internal.CLibrary")
    }
}

private fun Project.compilerShadowJar(taskName: String, body: ShadowJar.() -> Unit): TaskProvider<ShadowJar> {

    konst compilerJar = configurations.getOrCreate("compilerJar").apply {
        isCanBeConsumed = false
        isCanBeResolved = true
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        }
    }

    dependencies.add(compilerJar.name, dependencies.project(":kotlin-compiler")) { isTransitive = false }

    return tasks.register<ShadowJar>(taskName) {
        destinationDirectory.set(project.file(File(buildDir, "libs")))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(compilerJar)
        body()
    }
}

fun Project.embeddableCompiler(taskName: String = "embeddable", body: ShadowJar.() -> Unit = {}): TaskProvider<ShadowJar> =
    compilerShadowJar(taskName) {
        configureEmbeddableCompilerRelocation()
        body()
    }

fun Project.compilerDummyForDependenciesRewriting(
    taskName: String = "compilerDummy", body: ShadowJar.() -> Unit = {}
): TaskProvider<out Jar> =
    compilerShadowJar(taskName) {
        exclude(packagesToExcludeFromDummy)
        body()
    }
const konst COMPILER_DUMMY_JAR_CONFIGURATION_NAME = "compilerDummyJar"

fun Project.compilerDummyJar(task: TaskProvider<out Jar>, body: Jar.() -> Unit = {}) {
    configurations.getOrCreate(COMPILER_DUMMY_JAR_CONFIGURATION_NAME).apply {
        isCanBeResolved = false
        isCanBeConsumed = true
    }

    task.configure(body)
    addArtifact(COMPILER_DUMMY_JAR_CONFIGURATION_NAME, task)
}

const konst EMBEDDABLE_COMPILER_TASK_NAME = "embeddable"
fun Project.embeddableCompilerDummyForDependenciesRewriting(
    taskName: String = EMBEDDABLE_COMPILER_TASK_NAME,
    body: ShadowJar.() -> Unit = {}
): TaskProvider<ShadowJar> {
    konst compilerDummyJar = configurations.getOrCreate(COMPILER_DUMMY_JAR_CONFIGURATION_NAME).apply {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    dependencies.add(
        compilerDummyJar.name,
        dependencies.project(":kotlin-compiler-embeddable", configuration = COMPILER_DUMMY_JAR_CONFIGURATION_NAME)
    )

    return tasks.register<ShadowJar>(taskName) {
        destinationDirectory.set(project.file(File(buildDir, "libs")))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(compilerDummyJar)
        configureEmbeddableCompilerRelocation(withJavaxInject = false)
        body()
    }
}

fun Project.rewriteDepsToShadedJar(
    originalJarTask: TaskProvider<out Jar>, shadowJarTask: TaskProvider<ShadowJar>, body: Jar.() -> Unit = {}
): TaskProvider<ShadowJar> {
    originalJarTask.configure {
        archiveClassifier.set("original")
    }


    shadowJarTask.configure {
        dependsOn(originalJarTask)
        from(originalJarTask)// { include("**") }

        // When Gradle traverses the inputs, reject the shaded compiler JAR,
        // which leads to the content of that JAR being excluded as well:
        exclude {
            // Docstring says `file` never returns null, but it does
            @Suppress("UNNECESSARY_SAFE_CALL", "SAFE_CALL_WILL_CHANGE_NULLABILITY")
            it.file?.name?.startsWith("kotlin-compiler-embeddable") ?: false
        }

        archiveClassifier.set("original")
        body()
    }
    return shadowJarTask
}

fun Project.rewriteDepsToShadedCompiler(originalJarTask: TaskProvider<out Jar>, body: Jar.() -> Unit = {}): TaskProvider<ShadowJar> =
    rewriteDepsToShadedJar(originalJarTask, embeddableCompilerDummyForDependenciesRewriting(), body)

fun Project.rewriteDefaultJarDepsToShadedCompiler(body: Jar.() -> Unit = {}): TaskProvider<ShadowJar> =
    rewriteDepsToShadedJar(tasks.named<Jar>("jar"), embeddableCompilerDummyForDependenciesRewriting(), body)
