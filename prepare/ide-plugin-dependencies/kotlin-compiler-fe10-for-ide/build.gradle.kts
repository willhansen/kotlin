plugins {
    kotlin("jvm")
}

konst fe10CompilerModules: Array<String> by rootProject.extra

konst excludedCompilerModules = listOf(
    ":compiler:cli",
    ":compiler:cli-js",
    ":compiler:javac-wrapper",
    ":compiler:incremental-compilation-impl"
)

konst projects = fe10CompilerModules.asList() - excludedCompilerModules + listOf(":analysis:kt-references:kt-references-fe10")

publishJarsForIde(projects)
