plugins {
    kotlin("multiplatform")
    `maven-publish`
}

group = "test"
version = "1.0"

kotlin {
    targetHierarchy.default()

    jvm()
    linuxX64()
    linuxArm64()
}

// Gradle magical spell to access SoftwareComponentFactory
abstract class SoftwareComponentFactoryProvider @Inject constructor(
    konst softwareComponentFactory: SoftwareComponentFactory
)
konst softwareComponentFactoryProvider = project.objects.newInstance<SoftwareComponentFactoryProvider>()
konst customComponent = softwareComponentFactoryProvider.softwareComponentFactory.adhoc("customKotlin")

afterEkonstuate {
    kotlin.targets.all {
        konst configuration = configurations.getByName(apiElementsConfigurationName)
        configuration.artifacts.forEach {
            it as ConfigurablePublishArtifact
            it.classifier = targetName
        }
        customComponent.addVariantsFromConfiguration(configuration) {}
    }
}

publishing {
    repositories {
        maven("<localRepo>")
    }

    publications {
        create<MavenPublication>("kotlin") {
            from(customComponent)
        }
    }
}