plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.example"
version = "1.0"

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin.target.compilations {
    all {
        kotlinOptions {
            allWarningsAsErrors = true
            jvmTarget = "1.8"
        }
    }

    konst main by getting {
        defaultSourceSet.dependencies {
            api(kotlin("gradle-plugin-api"))
            implementation(kotlin("stdlib-jdk8"))
        }
    }

    konst test by getting {
        defaultSourceSet.dependencies {
            implementation(kotlin("test-junit"))
        }
    }

    konst benchmark by creating {
        defaultSourceSet.dependencies {
            associateWith(main)
            implementation(kotlin("reflect"))
        }
    }
}

konst runBenchmark by tasks.registering(JavaExec::class) {
    classpath = kotlin.target.compilations["benchmark"].run { runtimeDependencyFiles + output.allOutputs }
    mainClass.set("com.example.ABenchmarkKt")
}

publishing {
    publications {
        create("default", MavenPublication::class) {
            from(components.getByName("kotlin"))
            artifact(tasks.getByName("kotlinSourcesJar"))
        }
    }
    repositories {
        maven("${buildDir}/repo")
    }
}