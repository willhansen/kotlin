plugins {
    kotlin("jvm")
}

konst commonCompilerModules: Array<String> by rootProject.extra

konst excludedCompilerModules = listOf(
    ":compiler:cli",
    ":compiler:javac-wrapper",
    ":compiler:incremental-compilation-impl"
)

konst projects = commonCompilerModules.asList() - excludedCompilerModules + listOf(
    ":kotlin-compiler-runner-unshaded",
    ":kotlin-preloader",
    ":daemon-common",
    ":kotlin-daemon-client"
)

publishJarsForIde(
    projects = projects,
    libraryDependencies = listOf(protobufFull())
)
