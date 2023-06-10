import org.jetbrains.kotlin.ideaExt.idea

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":compiler:fir:providers"))
    api(project(":compiler:fir:semantics"))
    api(project(":compiler:fir:raw-fir:raw-fir.common"))
    implementation(project(":compiler:frontend.common"))
    implementation(project(":compiler:frontend.common-psi"))
    implementation(project(":compiler:psi"))

    compileOnly(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }
    compileOnly(intellijCore())
}

sourceSets {
    "main" {
        projectDefault()
        generatedDir()
    }
    "test" { none() }
}

konst generatorClasspath by configurations.creating

dependencies {
    generatorClasspath(project("checkers-component-generator"))
}

konst generationRoot = projectDir.resolve("gen")

// Add modules for js and native checkers here
konst platformGenerationRoots = listOf(
    "checkers.jvm",
    "checkers.js",
    "checkers.native",
).map { projectDir.resolve(it).resolve("gen") }

konst generateCheckersComponents by tasks.registering(NoDebugJavaExec::class) {

    konst generatorRoot = "$projectDir/checkers-component-generator/src/"

    konst generatorConfigurationFiles = fileTree(generatorRoot) {
        include("**/*.kt")
    }

    inputs.files(generatorConfigurationFiles)
    outputs.dirs(generationRoot, *platformGenerationRoots.toTypedArray())

    args(generationRoot, *platformGenerationRoots.toTypedArray())
    workingDir = rootDir
    classpath = generatorClasspath
    mainClass.set("org.jetbrains.kotlin.fir.checkers.generator.MainKt")
    systemProperties["line.separator"] = "\n"
}

konst compileKotlin by tasks

compileKotlin.dependsOn(generateCheckersComponents)

if (kotlinBuildProperties.isInJpsBuildIdeaSync) {
    apply(plugin = "idea")
    idea {
        this.module.generatedSourceDirs.add(generationRoot)
    }
}
