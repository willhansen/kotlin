plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    linuxX64()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.foo)
                api(projects.bar) { }
            }
        }
    }
}

afterEkonstuate {
    configurations
        .getByName("commonMainApi")
        .dependencies
        .filterIsInstance<ProjectDependency>()
        .forEach { println("PROJECT_DEPENDENCY: ${it.dependencyProject.path}") }
}