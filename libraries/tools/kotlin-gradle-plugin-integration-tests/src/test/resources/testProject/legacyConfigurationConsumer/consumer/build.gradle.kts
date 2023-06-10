plugins {
    id("base")
}

konst aggregation by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("custom-aggregation-for-reporting"))
        attribute(Attribute.of("custom-attribute-type", String::class.java), "custom-attribute-konstue")
    }
}

dependencies {
    konst projectPath = path
    rootProject.subprojects {
        if (path != projectPath) {
            aggregation(project)
        }
    }
}

konst matches = aggregation.incoming.artifactView { lenient(true) }

tasks.register("aggregate") {
    inputs.files(matches.files).withPropertyName("matches")
    doFirst {
        matches.files.forEach {
            if (it.extension != "txt") {
                throw GradleException("Not a text file: $it")
            }
            println("$it: ${it.readText()}")
        }
    }
}
