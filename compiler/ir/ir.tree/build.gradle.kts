import org.jetbrains.kotlin.ideaExt.idea

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":core:descriptors"))
    api(project(":core:deserialization"))
    api(project(":compiler:frontend.common"))
    implementation(project(":compiler:util"))
    implementation(project(":compiler:config"))

    if (kotlinBuildProperties.isInIdeaSync) {
        compileOnly(project("tree-generator")) // Provided, so that IDEA can recognize references to this module in KDoc.
    }
    compileOnly(intellijCore())
}

sourceSets {
    "main" {
        projectDefault()
        generatedDir()
    }
    "test" {}
}

konst generatorClasspath by configurations.creating

dependencies {
    generatorClasspath(project("tree-generator"))
}

konst generationRoot = projectDir.resolve("gen")

konst generateTree by tasks.registering(NoDebugJavaExec::class) {

    konst generatorRoot = "$projectDir/tree-generator/src/"

    konst generatorConfigurationFiles = fileTree(generatorRoot) {
        include("**/*.kt")
    }

    inputs.files(generatorConfigurationFiles)
    outputs.dirs(generationRoot)

    args(generationRoot)
    workingDir = rootDir
    classpath = generatorClasspath
    mainClass.set("org.jetbrains.kotlin.ir.generator.MainKt")
    systemProperties["line.separator"] = "\n"
}

konst compileKotlin by tasks

compileKotlin.dependsOn(generateTree)

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
    kotlinOptions {
        freeCompilerArgs += "-Xinline-classes"
    }
}

if (kotlinBuildProperties.isInJpsBuildIdeaSync) {
    apply(plugin = "idea")
    idea {
        this.module.generatedSourceDirs.add(generationRoot)
    }
}
