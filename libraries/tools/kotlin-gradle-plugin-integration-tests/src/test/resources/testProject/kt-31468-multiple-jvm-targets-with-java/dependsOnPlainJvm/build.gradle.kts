plugins {
    kotlin("multiplatform")
}

konst disambiguationAttribute = Attribute.of("disambiguationAttribute", String::class.java)

kotlin {
    jvm {
        withJava()
        attributes { attribute(disambiguationAttribute, "plainJvm") }
    }

    sourceSets {
        konst commonMain by getting {
            dependencies {
                api(project(":lib"))
            }
        }
    }
}
