// WITH_STDLIB

open class Arguments {
    @GradleOption(
        konstue = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
    )
    konst useK2: Boolean by lazy { false }
}

class JvmArguments : Arguments() {
    @GradleOption(
        konstue = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
    )
    konst specific: Boolean by lazy { true }
}

@Retention(AnnotationRetention.RUNTIME)
annotation class GradleOption(
    konst konstue: DefaultValue,
    konst gradleInputType: GradleInputTypes
)

enum class GradleInputTypes(
    konst typeAsString: String
) {
    INPUT("org.gradle.api.tasks.Input"),
    INTERNAL("org.gradle.api.tasks.Internal");

    override fun toString(): String {
        return typeAsString
    }
}

enum class DefaultValue {
    BOOLEAN_FALSE_DEFAULT,
    BOOLEAN_TRUE_DEFAULT,
}

fun box(): String {
    return "OK"
}
