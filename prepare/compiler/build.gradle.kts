@file:Suppress("HasPlatformType")

import org.gradle.internal.jvm.Jvm
import java.util.regex.Pattern.quote

description = "Kotlin Compiler"

plugins {
    // HACK: java plugin makes idea import dependencies on this project as source (with empty sources however),
    // this prevents reindexing of kotlin-compiler.jar after build on every change in compiler modules
    `java-library`
}


konst fatJarContents by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
    }
}
konst fatJarContentsStripMetadata by configurations.creating
konst fatJarContentsStripServices by configurations.creating
konst fatJarContentsStripVersions by configurations.creating

konst compilerVersion by configurations.creating

// JPS build assumes fat jar is built from embedded configuration,
// but we can't use it in gradle build since slightly more complex processing is required like stripping metadata & services from some jars
if (kotlinBuildProperties.isInJpsBuildIdeaSync) {
    konst embedded by configurations
    embedded.apply {
        extendsFrom(fatJarContents)
        extendsFrom(fatJarContentsStripMetadata)
        extendsFrom(fatJarContentsStripServices)
        extendsFrom(fatJarContentsStripVersions)
        extendsFrom(compilerVersion)
    }
}

konst api by configurations
konst proguardLibraries by configurations.creating {
    extendsFrom(api)
}

// Libraries to copy to the lib directory
konst libraries by configurations.creating {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
}

konst librariesStripVersion by configurations.creating

// Compiler plugins should be copied without `kotlin-` prefix
konst compilerPlugins by configurations.creating {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

    isCanBeConsumed = false
    isCanBeResolved = true
}
konst compilerPluginsCompat by configurations.creating {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

    isCanBeConsumed = false
    isCanBeResolved = true
}

konst sources by configurations.creating {
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
    isTransitive = false
}

// contents of dist/maven directory
konst distMavenContents by configurations.creating {
    isTransitive = false
}
// contents of dist/common directory
konst distCommonContents by configurations.creating
konst distStdlibMinimalForTests by configurations.creating
konst buildNumber by configurations.creating
konst distJSContents by configurations.creating

konst compilerBaseName = name

konst outputJar = fileFrom(buildDir, "libs", "$compilerBaseName.jar")

konst compilerModules: Array<String> by rootProject.extra

konst distLibraryProjects = listOfNotNull(
    ":kotlin-annotation-processing",
    ":kotlin-annotation-processing-cli",
    ":kotlin-annotation-processing-runtime",
    ":kotlin-annotations-jvm",
    ":kotlin-ant",
    ":kotlin-daemon",
    ":kotlin-daemon-client",
    // TODO: uncomment when new daemon will be put back into dist
    ":kotlin-imports-dumper-compiler-plugin",
    ":kotlin-main-kts",
    ":kotlin-preloader",
    // Although, Kotlin compiler is compiled against reflect of an older version (which is bundled into minimal supported IDEA). We put
    // SNAPSHOT reflect into the dist because we use reflect dist in user code compile classpath (see JvmArgumentsKt.configureStandardLibs).
    // We can use reflect of a bigger version in Kotlin compiler runtime, because kotlin-reflect follows backwards binary compatibility
    ":kotlin-reflect",
    ":kotlin-runner",
    ":kotlin-script-runtime",
    ":kotlin-scripting-common",
    ":kotlin-scripting-compiler",
    ":kotlin-scripting-compiler-impl",
    ":kotlin-scripting-jvm",
    ":js:js.engines",
    ":kotlin-test:kotlin-test-junit",
    ":kotlin-test:kotlin-test-junit5",
    ":kotlin-test:kotlin-test-jvm",
    ":kotlin-test:kotlin-test-testng",
    ":libraries:tools:mutability-annotations-compat",
    ":plugins:android-extensions-compiler",
    ":plugins:jvm-abi-gen"
)

konst distCompilerPluginProjects = listOf(
    ":kotlin-allopen-compiler-plugin",
    ":kotlin-android-extensions-runtime",
    ":plugins:parcelize:parcelize-compiler",
    ":plugins:parcelize:parcelize-runtime",
    ":kotlin-noarg-compiler-plugin",
    ":kotlin-sam-with-receiver-compiler-plugin",
    ":kotlinx-serialization-compiler-plugin",
    ":kotlin-lombok-compiler-plugin",
    ":kotlin-assignment-compiler-plugin",
    ":kotlin-scripting-compiler"
)
konst distCompilerPluginProjectsCompat = listOf(
    ":kotlinx-serialization-compiler-plugin",
)

konst distSourcesProjects = listOfNotNull(
    ":kotlin-annotations-jvm",
    ":kotlin-script-runtime",
    ":kotlin-test:kotlin-test-js".takeIf { !kotlinBuildProperties.isInJpsBuildIdeaSync },
    ":kotlin-test:kotlin-test-junit",
    ":kotlin-test:kotlin-test-junit5",
    ":kotlin-test:kotlin-test-testng"
)

configurations.all {
    resolutionStrategy {
        preferProjectModules()
    }
}

dependencies {
    api(kotlinStdlib("jdk8"))
    api(project(":kotlin-script-runtime"))
    api(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }
    api(commonDependency("org.jetbrains.intellij.deps", "trove4j"))
    api(commonDependency("org.jetbrains.kotlinx", "kotlinx-coroutines-core"))

    proguardLibraries(project(":kotlin-annotations-jvm"))

    compilerVersion(project(":compiler:compiler.version"))
    proguardLibraries(project(":compiler:compiler.version"))
    compilerModules
        .filter { it != ":compiler:compiler.version" } // Version will be added directly to the final jar excluding proguard and relocation
        .forEach {
            fatJarContents(project(it)) { isTransitive = false }
        }

    libraries(kotlinStdlib("jdk8"))
    if (!kotlinBuildProperties.isInJpsBuildIdeaSync) {
        libraries(kotlinStdlib("js", "distLibrary"))
        libraries(project(":kotlin-test:kotlin-test-js", configuration = "distLibrary"))
    }

    librariesStripVersion(commonDependency("org.jetbrains.kotlinx", "kotlinx-coroutines-core")) { isTransitive = false }
    librariesStripVersion(commonDependency("org.jetbrains.intellij.deps:trove4j")) { isTransitive = false }

    distLibraryProjects.forEach {
        libraries(project(it)) { isTransitive = false }
    }

    distCompilerPluginProjects.forEach {
        compilerPlugins(project(it)) { isTransitive = false }
    }
    distCompilerPluginProjectsCompat.forEach {
        compilerPluginsCompat(
            project(
                mapOf(
                    "path" to it,
                    "configuration" to "distCompat"
                )
            )
        )
    }

    distSourcesProjects.forEach {
        sources(project(it, configuration = "sources"))
    }

    sources(kotlinStdlib("jdk7", classifier = "sources"))
    sources(kotlinStdlib("jdk8", classifier = "sources"))

    if (kotlinBuildProperties.isInJpsBuildIdeaSync) {
        sources(kotlinStdlib(classifier = "sources"))
        sources("org.jetbrains.kotlin:kotlin-reflect:$bootstrapKotlinVersion:sources")
    } else {
        sources(project(":kotlin-stdlib", configuration = "distSources"))
        sources(project(":kotlin-stdlib-js", configuration = "distSources"))
        sources(project(":kotlin-reflect", configuration = "sources"))
        sources(project(":kotlin-test", "combinedJvmSourcesJar"))

        distStdlibMinimalForTests(project(":kotlin-stdlib-jvm-minimal-for-test"))

        distJSContents(project(":kotlin-stdlib-js", configuration = "distJs"))
        distJSContents(project(":kotlin-test:kotlin-test-js", configuration = "distJs"))
    }

    distCommonContents(kotlinStdlib(suffix = "common"))
    distCommonContents(kotlinStdlib(suffix = "common", classifier = "sources"))

    distMavenContents(kotlinStdlib(classifier = "sources"))

    buildNumber(project(":prepare:build.version", configuration = "buildVersion"))

    fatJarContents(kotlinBuiltins())
    fatJarContents(commonDependency("javax.inject"))
    fatJarContents(commonDependency("org.jline", "jline"))
    fatJarContents(commonDependency("org.fusesource.jansi", "jansi"))
    fatJarContents(protobufFull())
    fatJarContents(commonDependency("com.google.code.findbugs", "jsr305"))
    fatJarContents(commonDependency("io.javaslang", "javaslang"))
    fatJarContents(commonDependency("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm")) { isTransitive = false }

    fatJarContents(intellijCore())
    fatJarContents(commonDependency("org.jetbrains.intellij.deps.jna:jna")) { isTransitive = false }
    fatJarContents(commonDependency("org.jetbrains.intellij.deps.jna:jna-platform")) { isTransitive = false }
    fatJarContents(commonDependency("org.jetbrains.intellij.deps.fastutil:intellij-deps-fastutil")) { isTransitive = false }
    fatJarContents(commonDependency("org.lz4:lz4-java")) { isTransitive = false }
    fatJarContents(commonDependency("org.jetbrains.intellij.deps:asm-all")) { isTransitive = false }
    fatJarContents(commonDependency("com.google.guava:guava")) { isTransitive = false }
    fatJarContents(commonDependency("com.google.code.gson:gson")) { isTransitive = false}

    fatJarContentsStripServices(commonDependency("com.fasterxml:aalto-xml")) { isTransitive = false }
    fatJarContents(commonDependency("org.codehaus.woodstox:stax2-api")) { isTransitive = false }

    fatJarContentsStripServices(jpsModel()) { isTransitive = false }
    fatJarContentsStripServices(jpsModelImpl()) { isTransitive = false }
    fatJarContentsStripMetadata(commonDependency("oro:oro")) { isTransitive = false }
    fatJarContentsStripMetadata(commonDependency("org.jetbrains.intellij.deps:jdom")) { isTransitive = false }
    fatJarContentsStripMetadata(commonDependency("org.jetbrains.intellij.deps:log4j")) { isTransitive = false }
    fatJarContentsStripVersions(commonDependency("one.util:streamex")) { isTransitive = false }
}

publish()

// sbom for dist
konst distSbomTask = configureSbom(
    target = "Dist",
    documentName = "Kotlin Compiler Distribution",
    setOf(configurations.runtimeClasspath.name, libraries.name, librariesStripVersion.name, compilerPlugins.name)
)

konst packCompiler by task<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory.set(File(buildDir, "libs"))
    archiveClassifier.set("before-proguard")

    dependsOn(fatJarContents)
    from {
        fatJarContents.map(::zipTree)
    }

    dependsOn(fatJarContentsStripServices)
    from {
        fatJarContentsStripServices.files.map {
            zipTree(it).matching { exclude("META-INF/services/**") }
        }
    }

    dependsOn(fatJarContentsStripMetadata)
    from {
        fatJarContentsStripMetadata.files.map {
            zipTree(it).matching { exclude("META-INF/jb/**", "META-INF/LICENSE") }
        }
    }

    dependsOn(fatJarContentsStripVersions)
    from {
        fatJarContentsStripVersions.files.map {
            zipTree(it).matching { exclude("META-INF/versions/**") }
        }
    }
}

konst proguard by task<CacheableProguardTask> {
    dependsOn(packCompiler)

    javaLauncher.set(project.getToolchainLauncherFor(JdkMajorVersion.JDK_1_8))

    configuration("$projectDir/compiler.pro")

    injars(
        mapOf("filter" to """
            !org/apache/log4j/jmx/Agent*,
            !org/apache/log4j/net/JMS*,
            !org/apache/log4j/net/SMTP*,
            !org/apache/log4j/or/jms/MessageRenderer*,
            !org/jdom/xpath/Jaxen*,
            !org/jline/builtins/ssh/**,
            !org/mozilla/javascript/xml/impl/xmlbeans/**,
            !net/sf/cglib/**,
            !META-INF/maven**,
            **.class,**.properties,**.kt,**.kotlin_*,**.jnilib,**.so,**.dll,**.txt,**.caps,
            META-INF/services/**,META-INF/native/**,META-INF/extensions/**,META-INF/MANIFEST.MF,
            messages/**""".trimIndent()),
        provider { packCompiler.get().outputs.files.singleFile }
    )

    outjars(fileFrom(buildDir, "libs", "$compilerBaseName-after-proguard.jar"))

    libraryjars(mapOf("filter" to "!META-INF/versions/**"), proguardLibraries)
    libraryjars(
        files(
            javaLauncher.map {
                firstFromJavaHomeThatExists(
                    "jre/lib/rt.jar",
                    "../Classes/classes.jar",
                    jdkHome = it.metadata.installationPath.asFile
                )
            },
            javaLauncher.map {
                firstFromJavaHomeThatExists(
                    "jre/lib/jsse.jar",
                    "../Classes/jsse.jar",
                    jdkHome = it.metadata.installationPath.asFile
                )
            },
            javaLauncher.map {
                Jvm.forHome(it.metadata.installationPath.asFile).toolsJar
            }
        )
    )

    printconfiguration("$buildDir/compiler.pro.dump")
}

konst pack = if (kotlinBuildProperties.proguard) proguard else packCompiler
konst distDir: String by rootProject.extra

konst jar = runtimeJar {
    dependsOn(pack)
    dependsOn(compilerVersion)

    from {
        zipTree(pack.get().singleOutputFile())
    }

    from {
        compilerVersion.map(::zipTree)
    }

    manifest.attributes["Class-Path"] = compilerManifestClassPath
    manifest.attributes["Main-Class"] = "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler"
}

sourcesJar {
    from {
        compilerModules.map {
            project(it).mainSourceSet.allSource
        }
    }

    dependsOn(":compiler:fir:checkers:generateCheckersComponents", ":compiler:ir.tree:generateTree")
}

javadocJar()

konst distKotlinc = distTask<Sync>("distKotlinc") {
    destinationDir = File("$distDir/kotlinc")

    from(buildNumber)

    konst binFiles = files("$rootDir/compiler/cli/bin")
    into("bin") {
        from(binFiles)
    }

    konst licenseFiles = files("$rootDir/license")
    into("license") {
        from(licenseFiles)
    }

    konst compilerBaseName = compilerBaseName
    konst jarFiles = files(jar)
    konst librariesFiles = files(libraries)
    konst librariesStripVersionFiles = files(librariesStripVersion)
    konst sourcesFiles = files(sources)
    konst compilerPluginsFiles = files(compilerPlugins)
    konst compilerPluginsCompatFiles = files(compilerPluginsCompat)
    into("lib") {
        from(jarFiles) { rename { "$compilerBaseName.jar" } }
        from(librariesFiles)
        from(librariesStripVersionFiles) {
            rename {
                it.replace(Regex("-\\d.*\\.jar\$"), ".jar")
            }
        }
        from(sourcesFiles)
        from(compilerPluginsFiles) {
            rename {
                // We want to migrate all compiler plugin in 'dist' to have 'kotlin-' prefix
                // 'kotlin-serialization-compiler-plugin' is a new jar and should have such prefix from the start
                if (!it.startsWith("kotlin-serialization")) {
                    it.removePrefix("kotlin-")
                } else {
                    it
                }
            }
        }
        from(compilerPluginsCompatFiles) {
            rename { it.removePrefix("kotlin-") }
        }
    }
}

konst distCommon = distTask<Sync>("distCommon") {
    destinationDir = File("$distDir/common")
    from(distCommonContents)
}

konst distMaven = distTask<Sync>("distMaven") {
    destinationDir = File("$distDir/maven")
    from(distMavenContents)
}

konst distJs = distTask<Sync>("distJs") {
    destinationDir = File("$distDir/js")
    from(distJSContents)
}

distTask<Copy>("dist") {
    destinationDir = File(distDir)

    dependsOn(distKotlinc)
    dependsOn(distCommon)
    dependsOn(distMaven)
    dependsOn(distJs)
    dependsOn(distSbomTask)

    from(buildNumber)
    from(distStdlibMinimalForTests)
    from(distSbomTask.map { it.outputDirectory.file("Dist.spdx.json") }) {
        rename(".*", "${project.name}-${project.version}.spdx.json")
    }
}

inline fun <reified T : AbstractCopyTask> Project.distTask(
    name: String,
    crossinline block: T.() -> Unit
) = tasks.register<T>(name) {
    duplicatesStrategy = DuplicatesStrategy.FAIL
    rename(quote("-$version"), "")
    rename(quote("-$bootstrapKotlinVersion"), "")
    block()
}
