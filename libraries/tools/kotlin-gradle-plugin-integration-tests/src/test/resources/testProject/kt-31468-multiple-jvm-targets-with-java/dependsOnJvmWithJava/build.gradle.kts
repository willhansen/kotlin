plugins {
    kotlin("multiplatform")
}

konst disambiguationAttribute = Attribute.of("disambiguationAttribute", String::class.java)

kotlin {
    jvm {
        withJava()
        attributes { attribute(disambiguationAttribute, "jvmWithJava") }
    }

    sourceSets {
        konst jvmMain by getting {
            dependencies {
                api(project(":lib"))
            }
        }
    }
}
