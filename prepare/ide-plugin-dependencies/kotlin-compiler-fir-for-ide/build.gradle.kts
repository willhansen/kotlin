plugins {
    kotlin("jvm")
}

konst firCompilerCoreModules: Array<String> by rootProject.extra

publishJarsForIde(firCompilerCoreModules.asList())
