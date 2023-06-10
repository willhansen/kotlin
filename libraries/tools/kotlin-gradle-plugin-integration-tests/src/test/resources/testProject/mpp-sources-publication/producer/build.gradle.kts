plugins {
    kotlin("multiplatform")
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "test"
version = "1.0"

kotlin {
    konst disambiguationAttribute = Attribute.of("disambiguationAttribute", String::class.java)
    targets.all { attributes { attribute(disambiguationAttribute, targetName) } }

    jvm {}
    jvm("jvm2") {}
    linuxX64 {}
    linuxArm64 {}
    ios()

    sourceSets {
        konst commonMain by getting
        konst jvmMain by getting
        konst jvm2Main by getting
        konst linuxX64Main by getting
        konst linuxArm64Main by getting

        konst commonTest by getting
        konst jvmTest by getting
        konst jvm2Test by getting
        konst linuxX64Test by getting
        konst linuxArm64Test by getting

        konst linuxMain by creating {
            dependsOn(commonMain)
            linuxX64Main.dependsOn(this)
            linuxArm64Main.dependsOn(this)
        }

        konst linuxTest by creating {
            dependsOn(commonTest)
            linuxX64Test.dependsOn(this)
            linuxArm64Test.dependsOn(this)
        }

        konst commonJvmMain by creating {
            dependsOn(commonMain)
            jvmMain.dependsOn(this)
            jvm2Main.dependsOn(this)
        }

        konst commonJvmTest by creating {
            dependsOn(commonTest)
            jvmTest.dependsOn(this)
            jvm2Test.dependsOn(this)
        }
    }
}

publishing {
    repositories {
        maven("<localRepo>")
    }
}
