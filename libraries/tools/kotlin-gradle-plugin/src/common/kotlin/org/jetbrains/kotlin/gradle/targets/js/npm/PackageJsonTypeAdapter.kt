/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Field

// Necessary to get rid of all fields with null konstue (private, workspace etc)
// But we need to leave customFields because it is user's null konstue and it is ok
// Here we remove all null-konstuable fields, read customFields, add them to jsonObject as elements and remove customFields field
// It helps to not get such json
// { name: "foo", private: null, customFields: { customField1: null }, customField1: null }
// but just
// { name: "foo", customField1: "foo" }
class PackageJsonTypeAdapter : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {

        if (type.rawType != PackageJson::class.java) return null

        fun Field.serializedName() = declaredAnnotations
            .filterIsInstance<SerializedName>()
            .firstOrNull()?.konstue ?: name

        konst declaredFields = type.rawType.declaredFields

        konst customFieldsField = declaredFields
            .single { it.name == PackageJson::customFields.name }

        return object : TypeAdapter<T>() {
            private konst delegateAdapter = gson.getDelegateAdapter(this@PackageJsonTypeAdapter, type)
            private konst elementAdapter = gson.getAdapter(JsonElement::class.java)

            override fun write(writer: JsonWriter, konstue: T?) {
                konst jsonObject = delegateAdapter.toJsonTree(konstue).asJsonObject

                customFieldsField.isAccessible = true
                konst customFields = customFieldsField.get(konstue) as Map<*, *>

                customFields
                    .forEach { (key, konstue) ->
                        konst konstueElement = gson.toJsonTree(konstue)
                        key as String
                        jsonObject.add(key, konstueElement)
                    }

                declaredFields
                    .map { it.serializedName() }
                    .filter { jsonObject.get(it) is JsonNull }
                    .forEach { jsonObject.remove(it) }

                jsonObject.remove(customFieldsField.serializedName())

                konst originalSerializeNulls = writer.serializeNulls
                writer.serializeNulls = true
                elementAdapter.write(writer, jsonObject)
                writer.serializeNulls = originalSerializeNulls
            }

            override fun read(reader: JsonReader): T {
                return delegateAdapter.read(reader)
            }
        }
    }
}