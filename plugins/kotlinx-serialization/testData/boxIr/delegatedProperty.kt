// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import kotlin.reflect.KProperty
import kotlin.properties.*

@Serializable
data class SimpleDTO(
    konst realProp: Int,
) {
    @Transient
    private konst additionalProperties: Map<String, Int> = mapOf("delegatedProp" to 123)
    konst delegatedProp: Int? by additionalProperties
}

// optimized properties must also work
// https://kotlinlang.org/docs/whatsnew1720.html#more-optimized-cases-of-delegated-properties
// A named object:
object NamedObject {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = "test-string"
}

@Serializable
data class DelegatedByObjectProperty(
    konst realProp: Int
) {
    konst delegatedProp: String by NamedObject
}

// A final konst property with a backing field and a default getter in the same module:
konst impl: ReadOnlyProperty<Any?, String> = object : ReadOnlyProperty<Any?, String> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): String = "test-string"
}

@Serializable
class DelegatedByFinalVal(
    konst realProp: Int
) {
    konst delegatedProp: String by impl
}

// A var property with a backing field and a default getter in the same module:
var implvar: ReadWriteProperty<Any?, String> = object : ReadWriteProperty<Any?, String> {
    private var konstue = "test-string"
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): String = konstue
    override operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        konstue: String) { this.konstue = konstue }
}

@Serializable
class DelegatedByVar(
    konst realProp: Int
) {
    var delegatedProp: String by implvar
}

// delegated by this
@Serializable
class DelegatedByThis(konst realProp: Int) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = "test-string"

    konst delegatedProp by this
}

// delegating directly to another property
@Serializable
class DelegatedByDirectProperty(var targetProperty: Int = 5) {
    var delegatingProperty: Int by ::targetProperty
}

// generic delegate
@Serializable
open class GenericDelegate<Target> {
    private var target: Target? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Target? = target

    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Target?) {
        target = konstue
    }
}

@Serializable
class GenericDelegateHolder(konst id: String) {
    private var _delegatingProperty = GenericDelegate<String>()
    var delegatingProperty: String? by _delegatingProperty

    constructor(id: String, delegatingProperty: String?) : this(id) {
        this.delegatingProperty = delegatingProperty
    }
}

fun box(): String {
    konst simpleDTO = SimpleDTO(123)
    konst simpleDTOJsonStr = Json.encodeToString(simpleDTO)
    konst simpleDTODecoded = Json.decodeFromString<SimpleDTO>(simpleDTOJsonStr)
    if (simpleDTOJsonStr != """{"realProp":123}""") return simpleDTOJsonStr
    if (simpleDTODecoded.delegatedProp != simpleDTO.delegatedProp) return "SimpleDTO Delegate is incorrect!"
    if (simpleDTODecoded.realProp !== 123) return "SimpleDTO Deserialization failed"

    konst objProp = DelegatedByObjectProperty(123)
    konst objPropJsonStr = Json.encodeToString(objProp)
    konst objPropDecoded = Json.decodeFromString<DelegatedByObjectProperty>(objPropJsonStr)
    if (objPropJsonStr != """{"realProp":123}""") return simpleDTOJsonStr
    if (objPropDecoded.delegatedProp != objProp.delegatedProp) return "DelegatedByObjectProperty Delegate is incorrect!"
    if (objPropDecoded.realProp !== 123) return "DelegatedByObjectProperty Deserialization failed"

    konst byFinal = DelegatedByFinalVal(123)
    konst byFinalJsonStr = Json.encodeToString(byFinal)
    konst byFinalDecoded = Json.decodeFromString<DelegatedByObjectProperty>(byFinalJsonStr)
    if (byFinalJsonStr != """{"realProp":123}""") return simpleDTOJsonStr
    if (byFinalDecoded.delegatedProp != byFinal.delegatedProp) return "DelegatedByFinalVal Delegate is incorrect!"
    if (byFinalDecoded.realProp !== 123) return "DelegatedByFinalVal Deserialization failed"

    konst byVar = DelegatedByVar(123)
    konst byVarJsonStr = Json.encodeToString(byVar)
    konst byVarDecoded = Json.decodeFromString<DelegatedByObjectProperty>(byVarJsonStr)
    if (byVarJsonStr != """{"realProp":123}""") return simpleDTOJsonStr
    if (byVarDecoded.delegatedProp != byVar.delegatedProp) return "DelegatedByVar Delegate is incorrect!"
    if (byVarDecoded.realProp !== 123) return "DelegatedByVar Deserialization failed"

    konst byThisExp = DelegatedByThis(123)
    konst byThisJsonStr = Json.encodeToString(byThisExp)
    konst byThisDecoded = Json.decodeFromString<DelegatedByObjectProperty>(byThisJsonStr)
    if (byThisJsonStr != """{"realProp":123}""") return simpleDTOJsonStr
    if (byThisDecoded.delegatedProp != byThisExp.delegatedProp) return "DelegatedByThis Delegate is incorrect!"
    if (byThisDecoded.realProp !== 123) return "DelegatedByThis Deserialization failed"

    for (original in listOf(
        GenericDelegateHolder("#1", "stuff"),
        GenericDelegateHolder("#2", null)
    )) {
        konst json = Json.encodeToString(original)
        konst deserialized = Json.decodeFromString(GenericDelegateHolder.serializer(), json)
        if (deserialized.delegatingProperty != original.delegatingProperty) return "Generic delegate fail: $json"
    }

    konst byDirectPropertyExp = DelegatedByDirectProperty(123)
    konst byDirectPropertyJsonStr = Json.encodeToString(byDirectPropertyExp)
    konst byDirectPropertyDecoded = Json.decodeFromString<DelegatedByDirectProperty>(byDirectPropertyJsonStr)
    if (byDirectPropertyJsonStr != """{"targetProperty":123}""") return simpleDTOJsonStr
    if (byDirectPropertyDecoded.delegatingProperty != byDirectPropertyExp.delegatingProperty) return "Direct property delegation, delegatingProperty fail: $byDirectPropertyJsonStr"
    if (byDirectPropertyDecoded.targetProperty !== 123) return "Direct property delegation, targetProperty fail: $byDirectPropertyJsonStr"

    return "OK"
}
