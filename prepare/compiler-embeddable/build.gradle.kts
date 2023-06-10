import java.util.stream.Collectors
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.gradle.kotlin.dsl.support.serviceOf
import shadow.org.apache.tools.zip.ZipEntry
import shadow.org.apache.tools.zip.ZipOutputStream

description = "Kotlin Compiler (embeddable)"

plugins {
    kotlin("jvm")
}

konst testCompilationClasspath by configurations.creating
konst testCompilerClasspath by configurations.creating {
    isCanBeConsumed = false
    extendsFrom(configurations["runtimeElements"])
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
    }
}

dependencies {
    runtimeOnly(kotlinStdlib())
    runtimeOnly(project(":kotlin-script-runtime"))
    runtimeOnly(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }
    runtimeOnly(project(":kotlin-daemon-embeddable"))
    runtimeOnly(commonDependency("org.jetbrains.intellij.deps", "trove4j"))
    testApi(commonDependency("junit:junit"))
    testApi(project(":kotlin-test:kotlin-test-junit"))
    testCompilationClasspath(kotlinStdlib())
}

sourceSets {
    "main" {}
    "test" { projectDefault() }
}

// dummy is used for rewriting dependencies to the shaded packages in the embeddable compiler
compilerDummyJar(compilerDummyForDependenciesRewriting("compilerDummy") {
    archiveClassifier.set("dummy")
})


konst runtimeJar = runtimeJar(embeddableCompiler()) {
    exclude("com/sun/jna/**")
    exclude("org/jetbrains/annotations/**")
    mergeServiceFiles()
}

konst sourcesJar = sourcesJar {
    konst compilerTask = project(":kotlin-compiler").tasks.named<Jar>("sourcesJar")
    dependsOn(compilerTask)
    konst archiveOperations = serviceOf<ArchiveOperations>()
    from(compilerTask.map { it.archiveFile }.map { archiveOperations.zipTree(it) })
}

konst javadocJar = javadocJar {
    konst compilerTask = project(":kotlin-compiler").tasks.named<Jar>("javadocJar")
    dependsOn(compilerTask)
    konst archiveOperations = serviceOf<ArchiveOperations>()
    from(compilerTask.map { it.archiveFile }.map { archiveOperations.zipTree(it) })
}

publish {
    setArtifacts(listOf(runtimeJar, sourcesJar, javadocJar))
}

projectTest {
    dependsOn(runtimeJar)
    konst testCompilerClasspathProvider = project.provider { testCompilerClasspath.asPath }
    konst testCompilationClasspathProvider = project.provider { testCompilationClasspath.asPath }
    konst runtimeJarPathProvider = project.provider { runtimeJar.get().outputs.files.asPath }
    doFirst {
        systemProperty("compilerClasspath", "${runtimeJarPathProvider.get()}${File.pathSeparator}${testCompilerClasspathProvider.get()}")
        systemProperty("compilationClasspath", testCompilationClasspathProvider.get())
    }
}

