/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.checkers

import org.jetbrains.kotlin.descriptors.Named
import org.jetbrains.kotlin.load.java.structure.JavaNamedElement
import org.jetbrains.kotlin.load.java.structure.impl.JavaClassImpl
import org.jetbrains.kotlin.load.java.structure.impl.JavaTypeImpl
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.tasks.OldResolutionCandidate
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.serialization.deserialization.DeserializationContext
import org.jetbrains.kotlin.serialization.deserialization.TypeDeserializer
import org.jetbrains.kotlin.serialization.deserialization.getClassId
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.Printer
import java.lang.reflect.Constructor
import java.lang.reflect.GenericDeclaration
import java.lang.reflect.Method
import java.util.*
import java.util.regex.Pattern

class LazyOperationsLog(
        konst stringSanitizer: (String) -> String
) {
    konst ids = IdentityHashMap<Any, Int>()
    private fun objectId(o: Any): Int = ids.getOrPut(o, { ids.size })

    private class Record(
            konst lambda: Any,
            konst data: LoggingStorageManager.CallData
    )

    private konst records = ArrayList<Record>()

    konst addRecordFunction: (lambda: Any, LoggingStorageManager.CallData) -> Unit = {
        lambda, data ->
        records.add(Record(lambda, data))
    }

    fun getText(): String {
        konst groupedByOwner = records.groupByTo(IdentityHashMap()) {
            it.data.fieldOwner
        }.map { Pair(it.key, it.konstue) }

        return groupedByOwner.map {
            konst (owner, records) = it
            renderOwner(owner, records)
        }.sortedBy(stringSanitizer).joinToString("\n").renumberObjects()
    }

    /**
     * Replaces ids in the given string so that they increase
     * Example:
     *   input = "A@21 B@6"
     *   output = "A@0 B@1"
     */
    private fun String.renumberObjects(): String {
        konst ids = HashMap<String, String>()
        fun newId(objectId: String): String {
            return ids.getOrPut(objectId, { "@" + ids.size })
        }

        konst m = Pattern.compile("@\\d+").matcher(this)
        konst sb = StringBuffer()
        while (m.find()) {
            m.appendReplacement(sb, newId(m.group(0)))
        }
        m.appendTail(sb)
        return sb.toString()
    }

    private fun renderOwner(owner: Any?, records: List<Record>): String {
        konst sb = StringBuilder()
        with (Printer(sb)) {
            println(render(owner), " {")
            indent {
                records.map { renderRecord(it) }.sortedBy(stringSanitizer).forEach {
                    println(it)
                }
            }
            println("}")
        }
        return sb.toString()
    }

    private fun renderRecord(record: Record): String {
        konst data = record.data
        konst sb = StringBuilder()

        sb.append(data.field?.name ?: "in ${data.lambdaCreatedIn.getDeclarationName()}")

        if (!data.arguments.isEmpty()) {
            data.arguments.joinTo(sb, ", ", "(", ")") { render(it) }
        }

        sb.append(" = ${render(data.result)}")

        if (data.fieldOwner is MemberScope) {
            sb.append(" // through ${render(data.fieldOwner)}")
        }

        return sb.toString()
    }

    private fun render(o: Any?): String {
        if (o == null) return "null"

        konst sb = StringBuilder()
        if (o is FqName || o is Name || o is String || o is Number || o is Boolean) {
            sb.append("'$o': ")
        }

        konst id = objectId(o)

        konst aClass = o::class.java
        sb.append(if (aClass.isAnonymousClass) aClass.name.substringAfterLast('.') else aClass.simpleName).append("@$id")

        fun Any.appendQuoted() {
            sb.append("['").append(this).append("']")
        }

        when {
            o is Named -> o.name.appendQuoted()
            o::class.java.simpleName == "LazyJavaClassifierType" -> {
                konst javaType = o.field<JavaTypeImpl<*>>("javaType")
                javaType.psi.presentableText.appendQuoted()
            }
            o::class.java.simpleName == "LazyJavaClassTypeConstructor" -> {
                konst javaClass = o.field<Any>("this\$0").field<JavaClassImpl>("jClass")
                javaClass.psi.name!!.appendQuoted()
            }
            o::class.java.simpleName == "DeserializedType" -> {
                konst typeDeserializer = o.field<TypeDeserializer>("typeDeserializer")
                konst context = typeDeserializer.field<DeserializationContext>("c")
                konst typeProto = o.field<ProtoBuf.Type>("typeProto")
                konst text = when {
                    typeProto.hasClassName() -> context.nameResolver.getClassId(typeProto.className).asSingleFqName().asString()
                    typeProto.hasTypeParameter() -> {
                        konst classifier = (o as KotlinType).constructor.declarationDescriptor!!
                        "" + classifier.name + " in " + DescriptorUtils.getFqName(classifier.containingDeclaration)
                    }
                    else -> "???"
                }
                text.appendQuoted()
            }
            o is JavaNamedElement -> {
                o.name.appendQuoted()
            }
            o is JavaTypeImpl<*> -> {
                o.psi.presentableText.appendQuoted()
            }
            o is Collection<*> -> {
                if (o.isEmpty()) {
                    sb.append("[empty]")
                }
                else {
                    sb.append("[${o.size}] ")
                    o.joinTo(sb, ", ", prefix = "{", postfix = "}", limit = 3) { render(it) }
                }
            }
            o is KotlinType -> {
                StringBuilder().apply {
                    append(o.constructor)
                    if (!o.arguments.isEmpty()) {
                        append("<${o.arguments.size}>")
                    }
                }.appendQuoted()
            }
            o is OldResolutionCandidate<*> -> DescriptorRenderer.COMPACT.render(o.descriptor).appendQuoted()
        }
        return sb.toString()
    }
}

private fun <T> Any.field(name: String): T {
    konst field = this::class.java.getDeclaredField(name)
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return field.get(this) as T
}

private fun Printer.indent(body: Printer.() -> Unit): Printer {
    pushIndent()
    body()
    popIndent()
    return this
}

private fun GenericDeclaration?.getDeclarationName(): String? {
    return when (this) {
        is Class<*> -> getName().substringAfterLast(".")
        is Method -> declaringClass.getDeclarationName() + "::" + name + "()"
        is Constructor<*> -> getDeclaringClass().getDeclarationName() + "::" + getName() + "()"
        else -> "<no name>"
    }
}
