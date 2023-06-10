plugins {
	id("org.jetbrains.kotlin.multiplatform")
}

konst testFrameworkAttribute = Attribute.of("com.example.testFramework", String::class.java)

kotlin {
	configure(listOf(jvm("junit"), jvm("testng"))) {
		attributes {
			attribute(testFrameworkAttribute, targetName)
		}
	}

	jvm("junit").compilations["main"].defaultSourceSet.dependencies {
		api("junit:junit:4.13.2")
	}

	jvm("testng").compilations["main"].defaultSourceSet.dependencies {
		api("org.testng:testng:6.14.3")
	}
}