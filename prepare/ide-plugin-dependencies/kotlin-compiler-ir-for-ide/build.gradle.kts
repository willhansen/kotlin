plugins {
    kotlin("jvm")
}

konst irCompilerModules: Array<String> by rootProject.extra

publishJarsForIde(irCompilerModules.asList())
