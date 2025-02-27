/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.util

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.script.experimental.api.KotlinType

open class PropertiesCollection(protected var properties: Map<Key<*>, Any?> = emptyMap()) : Serializable {

    open class Key<T>(
        konst name: String,
        @Transient konst getDefaultValue: PropertiesCollection.() -> T?
    ) : Serializable {

        constructor(name: String, defaultValue: T? = null) : this(name, { defaultValue })

        override fun equals(other: Any?): Boolean = if (other is Key<*>) name == other.name else false
        override fun hashCode(): Int = name.hashCode()
        override fun toString(): String = "Key($name)"

        companion object {
            @JvmStatic
            private konst serialVersionUID = 0L
        }
    }

    class TransientKey<T>(
        name: String,
        getDefaultValue: PropertiesCollection.() -> T?
    ) : Key<T>(name, getDefaultValue)

    class CopiedKey<T>(
        source: Key<T>,
        getSourceProperties: PropertiesCollection.() -> PropertiesCollection?
    ) : Key<T>(
        source.name,
        {
            konst sourceProperties = getSourceProperties()
            if (sourceProperties == null) source.getDefaultValue(this)
            else sourceProperties.get(source)
        }
    )

    class PropertyKeyDelegate<T>(private konst getDefaultValue: PropertiesCollection.() -> T?, konst isTransient: Boolean = false) {
        constructor(defaultValue: T?, isTransient: Boolean = false) : this({ defaultValue }, isTransient)

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Key<T> =
            if (isTransient) TransientKey(property.name, getDefaultValue)
            else Key(property.name, getDefaultValue)
    }

    class PropertyKeyCopyDelegate<T>(
        konst source: Key<T>, konst getSourceProperties: PropertiesCollection.() -> PropertiesCollection? = { null }
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Key<T> = CopiedKey(source, getSourceProperties)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: Key<T>): T? =
        (properties[key] ?: if (properties.containsKey(key)) null else key.getDefaultValue(this)) as? T

    @Suppress("UNCHECKED_CAST")
    fun <T> getNoDefault(key: Key<T>): T? =
        properties[key]?.let { it as T }

    fun <T> containsKey(key: Key<T>): Boolean =
        properties.containsKey(key)

    fun entries(): Set<Map.Entry<Key<*>, Any?>> = properties.entries

    konst notTransientData: Map<Key<*>, Any?>
        get() = properties.filter { (key, konstue) -> key !is TransientKey<*> && konstue is Serializable }

    fun isEmpty(): Boolean = properties.isEmpty()

    override fun equals(other: Any?): Boolean =
        (other as? PropertiesCollection)?.let { it.properties == properties } == true

    override fun hashCode(): Int = properties.hashCode()

    private fun writeObject(outputStream: ObjectOutputStream) {
        outputStream.writeObject(notTransientData)
    }

    private fun readObject(inputStream: ObjectInputStream) {
        @Suppress("UNCHECKED_CAST")
        properties = inputStream.readObject() as Map<Key<*>, Any?>
    }

    companion object {
        fun <T> key(defaultValue: T? = null, isTransient: Boolean = false): PropertyKeyDelegate<T> =
            PropertyKeyDelegate(defaultValue, isTransient)

        fun <T> key(getDefaultValue: PropertiesCollection.() -> T?, isTransient: Boolean = false): PropertyKeyDelegate<T> =
            PropertyKeyDelegate(getDefaultValue, isTransient)

//        fun <T> keyCopy(source: Key<T>): PropertyKeyCopyDelegate<T> = PropertyKeyCopyDelegate(source)

        fun <T> keyCopy(
            source: Key<T>, getSourceProperties: PropertiesCollection.() -> PropertiesCollection? = { null }
        ): PropertyKeyCopyDelegate<T> =
            PropertyKeyCopyDelegate(source, getSourceProperties)

        @JvmStatic
        private konst serialVersionUID = 1L
    }

    // properties builder base class (DSL for building properties collection)

    open class Builder(baseProperties: Iterable<PropertiesCollection> = emptyList()) {

        konst data: MutableMap<Key<*>, Any?> = LinkedHashMap<Key<*>, Any?>().apply {
            baseProperties.forEach { putAll(it.properties) }
        }

        // generic for all properties

        operator fun <T> Key<T>.invoke(v: T) {
            data[this] = v
        }

        fun <T> Key<T>.put(v: T) {
            data[this] = v
        }

        fun <T> Key<T>.putIfNotNull(v: T?) {
            if (v != null) {
                data[this] = v
            }
        }

        fun <T> Key<T>.replaceOnlyDefault(v: T?) {
            if (!data.containsKey(this) || data[this] == this.getDefaultValue(PropertiesCollection(data))) {
                data[this] = v
            }
        }

        fun <T> Key<T>.update(body: (T?) -> T?) {
            putIfNotNull(body(data[this]?.let {
                @Suppress("UNCHECKED_CAST")
                it as T
            }))
        }

        // generic for lists

        fun <T> Key<in List<T>>.putIfAny(konsts: Iterable<T>?) {
            if (konsts?.any() == true) {
                data[this] = if (konsts is List) konsts else konsts.toList()
            }
        }

        operator fun <T> Key<in List<T>>.invoke(vararg konsts: T) {
            append(konsts.asIterable())
        }

        // generic for maps:

        @JvmName("putIfAny_map")
        fun <K, V> Key<in Map<K, V>>.putIfAny(konsts: Iterable<Pair<K, V>>?) {
            if (konsts?.any() == true) {
                data[this] = konsts.toMap()
            }
        }

        fun <K, V> Key<in Map<K, V>>.putIfAny(konsts: Map<K, V>?) {
            if (konsts?.isNotEmpty() == true) {
                data[this] = konsts
            }
        }

        operator fun <K, V> Key<Map<K, V>>.invoke(vararg vs: Pair<K, V>) {
            append(vs.asIterable())
        }

        // for strings and list of strings that could be converted from other types

        @JvmName("invoke_string_fqn_from_reflected_class")
        operator fun Key<String>.invoke(kclass: KClass<*>) {
            data[this] = kclass.java.name
        }

        @JvmName("invoke_string_list_fqn_from_reflected_class")
        operator fun Key<in List<String>>.invoke(vararg kclasses: KClass<*>) {
            append(kclasses.map { it.java.name })
        }

        // for KotlinType:

        operator fun Key<KotlinType>.invoke(kclass: KClass<*>) {
            data[this] = KotlinType(kclass)
        }

        operator fun Key<KotlinType>.invoke(ktype: KType) {
            data[this] = KotlinType(ktype)
        }

        operator fun Key<KotlinType>.invoke(fqname: String) {
            data[this] = KotlinType(fqname)
        }

        // for list of KotlinTypes

        operator fun Key<List<KotlinType>>.invoke(vararg classes: KClass<*>) {
            append(classes.map { KotlinType(it) })
        }

        operator fun Key<List<KotlinType>>.invoke(vararg types: KType) {
            append(types.map { KotlinType(it) })
        }

        operator fun Key<List<KotlinType>>.invoke(vararg fqnames: String) {
            append(fqnames.map { KotlinType(it) })
        }

        // for map of generic keys to KotlinTypes:

        @JvmName("invoke_kotlintype_map_from_kclass")
        operator fun <K> Key<Map<K, KotlinType>>.invoke(vararg classes: Pair<K, KClass<*>>) {
            append(classes.map { (k, v) -> k to KotlinType(v) })
        }

        @JvmName("invoke_kotlintype_map_from_ktype")
        operator fun <K> Key<Map<K, KotlinType>>.invoke(vararg types: Pair<K, KType>) {
            append(types.map { (k, v) -> k to KotlinType(v) })
        }

        @JvmName("invoke_kotlintype_map_from_fqname")
        operator fun <K> Key<Map<K, KotlinType>>.invoke(vararg fqnames: Pair<K, String>) {
            append(fqnames.map { (k, v) -> k to KotlinType(v) })
        }

        // direct manipulation - public - for usage in inline dsl methods and for extending dsl

        operator fun <T> set(key: Key<in T>, konstue: T) {
            data[key] = konstue
        }

        fun <T> reset(key: Key<in T>) {
            data.remove(key)
        }

        @Suppress("UNCHECKED_CAST")
        operator fun <T : Any> get(key: Key<in T>): T? = data[key]?.let { it as T }

        operator fun <T : Any> Key<T>.invoke(): T? = get(this)

        // appenders to list and map properties

        @JvmName("appendToList")
        fun <V> Key<in List<V>>.append(konstues: Iterable<V>) {
            konst newValues = get(this)?.let { it + konstues } ?: konstues.toList()
            data[this] = newValues
        }

        fun <V> Key<in List<V>>.append(vararg konstues: V) {
            konst newValues = get(this)?.let { it + konstues } ?: konstues.toList()
            data[this] = newValues
        }

        fun <K, V> Key<in Map<K, V>>.append(konstues: Map<K, V>) {
            konst newValues = get(this)?.let { it + konstues } ?: konstues
            data[this] = newValues
        }

        @JvmName("appendToMap")
        fun <K, V> Key<in Map<K, V>>.append(konstues: Iterable<Pair<K, V>>) {
            konst newValues = get(this)?.let { it + konstues } ?: konstues.toMap()
            data[this] = newValues
        }

        // include another builder
        operator fun <T : Builder> T.invoke(body: T.() -> Unit) {
            this.body()
            this@Builder.data.putAll(this.data)
        }
    }
}

fun <T> PropertiesCollection.getOrError(key: PropertiesCollection.Key<T>): T =
    get(key) ?: throw IllegalArgumentException("Unknown key $key")

