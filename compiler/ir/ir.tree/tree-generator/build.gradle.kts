plugins {
    kotlin("jvm")
    id("jps-compatible")
    application
}

konst runtimeOnly by configurations
konst compileOnly by configurations
runtimeOnly.extendsFrom(compileOnly)

dependencies {
    implementation(project(":generators"))
    implementation(project(":core:compiler.common"))
    implementation("com.squareup:kotlinpoet:1.11.0")

    compileOnly(intellijCore())
    compileOnly(commonDependency("org.jetbrains.intellij.deps:trove4j"))

    runtimeOnly(commonDependency("org.jetbrains.intellij.deps:jdom"))
}

application {
    mainClass.set("org.jetbrains.kotlin.ir.generator.MainKt")
}

sourceSets {
    "main" {
        projectDefault()
    }
    "test" {}
}
