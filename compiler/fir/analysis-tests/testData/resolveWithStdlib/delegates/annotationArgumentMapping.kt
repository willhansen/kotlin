import kotlin.contracts.*

class FirAnnotationArgumentMappingBuilder {
    konst mapping: MutableMap<String, String> = mutableMapOf()

    fun build(): FirAnnotationArgumentMapping {
        return FirAnnotationArgumentMapping(mapping)
    }
}

@OptIn(ExperimentalContracts::class)
fun buildAnnotationArgumentMapping(init: FirAnnotationArgumentMappingBuilder.() -> Unit): FirAnnotationArgumentMapping {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return FirAnnotationArgumentMappingBuilder().apply(init).build()
}

class FirAnnotationArgumentMapping(mapping: Map<String, String>)

class ValueParameter(konst name: String)
class Argument(konst name: String)

fun createArgumentMapping(
    konstueParameters: List<ValueParameter>?,
    arguments: List<Argument>
): FirAnnotationArgumentMapping {
    return buildAnnotationArgumentMapping build@{
        konst parameterByName: Map<String, ValueParameter>? by lazy {
            konst konstueParameters = konstueParameters ?: return@lazy null
            konstueParameters.associateBy { it.name }
        }

        arguments.mapNotNull {
            konst name = it.name
            konst konstue = parameterByName?.get(name)?.name ?: return@mapNotNull null
            name to konstue
        }.toMap(mapping)
    }
}
