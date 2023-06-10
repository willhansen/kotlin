import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.kotlinNativeDist

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

repositories {
    mavenCentral()
}

konst kotlinNativeEmbedded by configurations.creating
konst testPlugin by configurations.creating
konst testPluginRuntime by configurations.creating

fun DependencyHandlerScope.testPluginRuntime(any: Any) {
    konst notation = any as? String ?: return add(testPluginRuntime.name, any) {}
    konst (group, artifact, version) = notation.split(":")
    konst platformName = HostManager.host.name
    konst gradlePlatformName = platformName.replace("_", "")
    return add(testPluginRuntime.name, "$group:$artifact-$gradlePlatformName:$version") {
        isTransitive = false
        attributes {
            attribute(Attribute.of("artifactType", String::class.java), "org.jetbrains.kotlin.klib")
            attribute(Attribute.of("org.gradle.status", String::class.java), "release")
            attribute(Attribute.of("org.jetbrains.kotlin.native.target", String::class.java), platformName)
            attribute(Attribute.of("org.jetbrains.kotlin.platform.type", String::class.java), "native")
            attribute(Usage.USAGE_ATTRIBUTE, objects.named("kotlin-api"))
        }
    }
}

dependencies {
    kotlinNativeEmbedded(project(":kotlin-native:Interop:Runtime"))
    kotlinNativeEmbedded(project(":kotlin-native:Interop:Indexer"))
    kotlinNativeEmbedded(project(":kotlin-native:Interop:StubGenerator"))
    kotlinNativeEmbedded(project(":kotlin-native:Interop:Skia"))
    kotlinNativeEmbedded(project(":kotlin-native:backend.native"))
    kotlinNativeEmbedded(project(":kotlin-native:utilities:cli-runner"))
    kotlinNativeEmbedded(project(":kotlin-native:utilities:basic-utils"))
    kotlinNativeEmbedded(project(":kotlin-native:klib"))
    kotlinNativeEmbedded(project(":kotlin-native:endorsedLibraries:kotlinx.cli", "jvmRuntimeElements"))
    kotlinNativeEmbedded(project(":kotlin-compiler")) { isTransitive = false }
    testImplementation(commonDependency("junit:junit"))
    testImplementation(project(":kotlin-test:kotlin-test-junit"))
}

konst compiler = embeddableCompiler("kotlin-native-compiler-embeddable") {
    from(kotlinNativeEmbedded)
    /**
     * this jar distributed through kotlin-native distribution, but not with maven.
     */
    archiveVersion.set("")
    mergeServiceFiles()
}

konst runtimeJar = runtimeJar(compiler) {
    exclude("com/sun/jna/**")
    mergeServiceFiles()
}


kotlin.sourceSets["test"].kotlin.srcDir("tests/kotlin")


projectTest {
    /**
     * It's expected that test should be executed on CI, but currently this project under `kotlin.native.enabled`
     */
    dependsOn(runtimeJar)
    konst runtimeJarPathProvider = project.provider {
        konst jar = runtimeJar.get().outputs.files.asPath
        konst trove = configurations.detachedConfiguration(
                dependencies.module(commonDependency("org.jetbrains.intellij.deps:trove4j"))
        )
        (trove.files + jar).joinToString(File.pathSeparatorChar.toString())
    }
    doFirst {
        systemProperty("compilerClasspath", runtimeJarPathProvider.get())
        systemProperty("kotlin.native.home", kotlinNativeDist)
    }
}