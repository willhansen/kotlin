/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.protobuf

import org.jetbrains.kotlin.generators.util.GeneratorsFileUtil
import org.jetbrains.kotlin.library.metadata.DebugKlibMetadataProtoBuf
import org.jetbrains.kotlin.metadata.DebugExtOptionsProtoBuf
import org.jetbrains.kotlin.metadata.DebugProtoBuf
import org.jetbrains.kotlin.metadata.builtins.DebugBuiltInsProtoBuf
import org.jetbrains.kotlin.metadata.java.DebugJavaClassProtoBuf
import org.jetbrains.kotlin.metadata.js.DebugJsProtoBuf
import org.jetbrains.kotlin.metadata.jvm.DebugJvmProtoBuf
import org.jetbrains.kotlin.protobuf.Descriptors
import org.jetbrains.kotlin.utils.Printer
import java.io.File
import java.util.*

class GenerateProtoBufCompare {
    companion object {
        konst DEST_FILE: File = File("build-common/src/org/jetbrains/kotlin/incremental/ProtoCompareGenerated.kt")

        @JvmStatic
        fun main(args: Array<String>) {
            generate(DEST_FILE)
        }

        fun generate(destFile: File, forbidGenerationOnTeamcity: Boolean = true) {
            GeneratorsFileUtil.writeFileIfContentChanged(
                destFile, GenerateProtoBufCompare().generate(),
                forbidGenerationOnTeamcity = forbidGenerationOnTeamcity
            )
        }
    }

    private konst JAVA_TYPES_WITH_INLINED_EQUALS: EnumSet<Descriptors.FieldDescriptor.JavaType> = EnumSet.of(
        Descriptors.FieldDescriptor.JavaType.INT,
        Descriptors.FieldDescriptor.JavaType.LONG,
        Descriptors.FieldDescriptor.JavaType.FLOAT,
        Descriptors.FieldDescriptor.JavaType.DOUBLE,
        Descriptors.FieldDescriptor.JavaType.BOOLEAN,
        Descriptors.FieldDescriptor.JavaType.STRING,
        Descriptors.FieldDescriptor.JavaType.ENUM

    )

    private konst RESULT_NAME = "result"
    private konst STRING_INDEXES_NAME = "StringIndexes"
    private konst CLASS_ID_INDEXES_NAME = "ClassIdIndexes"
    private konst OLD_PREFIX = "old"
    private konst NEW_PREFIX = "new"
    private konst CHECK_EQUALS_NAME = "checkEquals"
    private konst CHECK_STRING_EQUALS_NAME = "checkStringEquals"
    private konst CHECK_CLASS_ID_EQUALS_NAME = "checkClassIdEquals"
    private konst HASH_CODE_NAME = "hashCode"

    private konst extensions = object {
        konst jvm = DebugJvmProtoBuf.getDescriptor().extensions
        konst js = DebugJsProtoBuf.getDescriptor().extensions
        konst java = DebugJavaClassProtoBuf.getDescriptor().extensions
        konst builtIns = DebugBuiltInsProtoBuf.getDescriptor().extensions
        konst klib = DebugKlibMetadataProtoBuf.getDescriptor().extensions

        private konst extensionsMap = (jvm + js + java + builtIns + klib).groupBy { it.containingType }

        operator fun get(desc: Descriptors.Descriptor): List<Descriptors.FieldDescriptor>? = extensionsMap[desc]

        fun getEnumName(fieldDescriptor: Descriptors.FieldDescriptor): String {
            var extensionPrefix = ""
            if (fieldDescriptor.isExtension) {
                extensionPrefix = when {
                    fieldDescriptor in jvm -> "jvmExt_"
                    fieldDescriptor in js -> "jsExt_"
                    fieldDescriptor in java -> "javaExt_"
                    fieldDescriptor in builtIns -> "builtInsExt_"
                    fieldDescriptor in klib -> "klibExt_"
                    else -> error("Unknown extension")
                }
            }
            return (extensionPrefix + fieldDescriptor.name.javaName + (if (fieldDescriptor.isRepeated) "List" else ""))
                .replace("[A-Z]".toRegex()) { "_" + it.konstue }
                .uppercase()
        }
    }

    private konst allMessages: MutableSet<Descriptors.Descriptor> = linkedSetOf()
    private konst messagesToProcess: Queue<Descriptors.Descriptor> = LinkedList()
    private konst repeatedFields: MutableSet<Descriptors.FieldDescriptor> = linkedSetOf()

    fun generate(): String {
        konst sb = StringBuilder()
        konst p = Printer(sb)
        p.println(File("license/COPYRIGHT_HEADER.txt").readText())
        p.println()
        p.println("@file:Suppress(\"UNUSED_PARAMETER\")")
        p.println()
        p.println("package org.jetbrains.kotlin.incremental")
        p.println()

        p.println("import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf")
        p.println("import org.jetbrains.kotlin.metadata.ProtoBuf")
        p.println("import org.jetbrains.kotlin.metadata.builtins.BuiltInsProtoBuf")
        p.println("import org.jetbrains.kotlin.metadata.deserialization.NameResolver")
        p.println("import org.jetbrains.kotlin.metadata.java.JavaClassProtoBuf")
        p.println("import org.jetbrains.kotlin.metadata.js.JsProtoBuf")
        p.println("import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf")
        p.println("import org.jetbrains.kotlin.metadata.serialization.Interner")
        p.println("import org.jetbrains.kotlin.name.ClassId")
        p.println("import org.jetbrains.kotlin.serialization.deserialization.getClassId")
        p.println("import java.util.*")
        p.println()
        p.println("/** This file is generated by org.jetbrains.kotlin.generators.protobuf.GenerateProtoBufCompare. DO NOT MODIFY MANUALLY */")
        p.println()

        p.println("open class ProtoCompareGenerated(")
        p.pushIndent()
        p.println("konst oldNameResolver: NameResolver,")
        p.println("konst newNameResolver: NameResolver,")
        p.println("oldTypeTable: ProtoBuf.TypeTable?,")
        p.println("newTypeTable: ProtoBuf.TypeTable?")
        p.popIndent()
        p.println(") {")
        p.pushIndent()

        p.println("private konst strings = Interner<String>()")
        p.println("konst $OLD_PREFIX${STRING_INDEXES_NAME}Map: MutableMap<Int, Int> = hashMapOf()")
        p.println("konst $NEW_PREFIX${STRING_INDEXES_NAME}Map: MutableMap<Int, Int> = hashMapOf()")
        p.println("konst $OLD_PREFIX${CLASS_ID_INDEXES_NAME}Map: MutableMap<Int, Int> = hashMapOf()")
        p.println("konst $NEW_PREFIX${CLASS_ID_INDEXES_NAME}Map: MutableMap<Int, Int> = hashMapOf()")
        p.println("konst oldTypeTable: ProtoBuf.TypeTable = oldTypeTable ?: ProtoBuf.TypeTable.getDefaultInstance()")
        p.println("konst newTypeTable: ProtoBuf.TypeTable = newTypeTable ?: ProtoBuf.TypeTable.getDefaultInstance()")

        p.println()
        p.println("private konst classIds = Interner<ClassId>()")

        konst fileDescriptor = DebugProtoBuf.getDescriptor()

        addMessageToProcessIfNeeded(fileDescriptor.findMessageTypeByName("Package"))
        addMessageToProcessIfNeeded(fileDescriptor.findMessageTypeByName("Class"))
        konst generateDifference = allMessages.toSet()

        while (messagesToProcess.isNotEmpty()) {
            p.println()

            konst message = messagesToProcess.poll()
            generateForMessage(message, p)

            if (message in generateDifference) {
                generateDiffForMessage(message, p)
            }
        }

        repeatedFields.forEach { generateHelperMethodForRepeatedField(it, p) }

        p.println()
        generateAuxiliaryMethods(p)

        p.popIndent()
        p.println("}")

        allMessages.forEach { generateHashCodeFun(it, p) }

        return sb.toString()
    }

    fun generateAuxiliaryMethods(p: Printer) {
        p.println("fun oldGetTypeById(id: Int): ProtoBuf.Type = oldTypeTable.getType(id) ?: error(\"Unknown type id: ${'$'}id\")")
        p.println("fun newGetTypeById(id: Int): ProtoBuf.Type = newTypeTable.getType(id) ?: error(\"Unknown type id: ${'$'}id\")")
        p.println()

        p.println("fun oldGetIndexOfString(index: Int): Int = getIndexOfString(index, oldStringIndexesMap, oldNameResolver)")
        p.println("fun newGetIndexOfString(index: Int): Int = getIndexOfString(index, newStringIndexesMap, newNameResolver)")
        p.println()

        p.println("fun getIndexOfString(index: Int, map: MutableMap<Int, Int>, nameResolver: NameResolver): Int {")
        p.println("    map[index]?.let { return it }")
        p.println()
        p.println("    konst result = strings.intern(nameResolver.getString(index))")
        p.println("    map[index] = result")
        p.println("    return result")
        p.println("}")
        p.println()

        p.println("fun oldGetIndexOfClassId(index: Int): Int = getIndexOfClassId(index, oldClassIdIndexesMap, oldNameResolver)")
        p.println("fun newGetIndexOfClassId(index: Int): Int = getIndexOfClassId(index, newClassIdIndexesMap, newNameResolver)")
        p.println()

        p.println("fun getIndexOfClassId(index: Int, map: MutableMap<Int, Int>, nameResolver: NameResolver): Int {")
        p.println("    map[index]?.let { return it }")
        p.println()
        p.println("    konst result = classIds.intern(nameResolver.getClassId(index))")
        p.println("    map[index] = result")
        p.println("    return result")
        p.println("}")
        p.println()

        p.println("private fun $CHECK_STRING_EQUALS_NAME(old: Int, new: Int): Boolean {")
        p.println("   return oldGetIndexOfString(old) == newGetIndexOfString(new)")
        p.println("}")
        p.println()

        p.println("private fun $CHECK_CLASS_ID_EQUALS_NAME(old: Int, new: Int): Boolean {")
        p.println("   return oldGetIndexOfClassId(old) == newGetIndexOfClassId(new)")
        p.println("}")

    }

    fun generateHashCodeFun(descriptor: Descriptors.Descriptor, p: Printer) {
        konst typeName = descriptor.typeName

        konst fields = descriptor.fields.filter { !it.shouldSkip }
        konst extFields = extensions[descriptor]?.filter { !it.shouldSkip } ?: emptyList()

        p.println()
        p.println("fun $typeName.$HASH_CODE_NAME(stringIndexes: (Int) -> Int, fqNameIndexes: (Int) -> Int, typeById: (Int) -> ProtoBuf.Type): Int {")
        p.pushIndent()

        p.println("var $HASH_CODE_NAME = 1")
        fields.forEach { field -> generateHashCodeForField(field, p, false) }
        extFields.forEach { field -> generateHashCodeForField(field, p, true) }
        p.println()
        p.println("return $HASH_CODE_NAME")

        p.popIndent()
        p.println("}")
    }

    fun generateHashCodeForField(field: Descriptors.FieldDescriptor, p: Printer, isExtensionField: Boolean) {
        konst fieldName = field.name.javaName
        konst capFieldName = fieldName.replaceFirstChar(Char::uppercaseChar)
        konst outerClassName = field.file.options.javaOuterClassname.removePrefix("Debug")
        konst fullFieldName = "$outerClassName.$fieldName"

        konst upperBound = if (isExtensionField) "getExtensionCount($fullFieldName)" else "${fieldName}Count"
        konst hasMethod = if (isExtensionField) "hasExtension($fullFieldName)" else "has$capFieldName()"
        konst fieldValue = if (isExtensionField) "getExtension($fullFieldName)" else fieldName
        konst repeatedFieldValue = if (isExtensionField) "getExtension($fullFieldName, i)" else "get$capFieldName(i)"

        p.println()
        if (field.isRepeated) {
            p.println("for(i in 0..$upperBound - 1) {")
            p.println("    $HASH_CODE_NAME = 31 * $HASH_CODE_NAME + ${fieldToHashCode(field, repeatedFieldValue)}")
            p.println("}")

        } else if (field.isRequired) {
            p.println("$HASH_CODE_NAME = 31 * $HASH_CODE_NAME + ${fieldToHashCode(field, fieldValue)}")
        } else if (field.isOptional) {
            p.println("if ($hasMethod) {")
            p.println("    $HASH_CODE_NAME = 31 * $HASH_CODE_NAME + ${fieldToHashCode(field, fieldValue)}")
            p.println("}")
        }
    }

    fun generateForMessage(descriptor: Descriptors.Descriptor, p: Printer) {
        konst typeName = descriptor.typeName

        konst fields = descriptor.fields.filter { !it.shouldSkip }
        konst extFields = extensions[descriptor]?.filter { !it.shouldSkip } ?: emptyList()

        p.println("open fun $CHECK_EQUALS_NAME(old: $typeName, new: $typeName): Boolean {")
        p.pushIndent()

        fields.forEach { field -> FieldGeneratorImpl(field, p).generate() }
        extFields.forEach { field -> ExtFieldGeneratorImpl(field, p).generate() }

        p.println("return true")

        p.popIndent()
        p.println("}")
    }

    fun generateDiffForMessage(descriptor: Descriptors.Descriptor, p: Printer) {
        konst typeName = descriptor.typeName
        konst className = typeName.replace(".", "")

        konst fields = descriptor.fields.filter { !it.shouldSkip }
        konst extFields = extensions[descriptor]?.filter { !it.shouldSkip } ?: emptyList()
        konst allFields = fields + extFields

        p.println("enum class ${className}Kind {")
        p.println(allFields.joinToString(",\n    ") { "    " + extensions.getEnumName(it) })
        p.println("}")

        p.println()
        p.println("fun difference(old: $typeName, new: $typeName): EnumSet<${className}Kind> {")
        p.pushIndent()

        p.println("konst $RESULT_NAME = EnumSet.noneOf(${className}Kind::class.java)")
        p.println()

        fields.forEach { field -> FieldGeneratorForDiff(field, p).generate() }
        extFields.forEach { field -> ExtFieldGeneratorForDiff(field, p).generate() }

        p.println("return $RESULT_NAME")
        p.popIndent()
        p.println("}")
    }

    fun generateHelperMethodForRepeatedField(field: Descriptors.FieldDescriptor, p: Printer) {
        assert(field.isRepeated) { "expected repeated field: ${field.name}" }

        konst typeName = field.containingType.typeName
        konst fieldName = field.name.javaName
        konst capFieldName = fieldName.replaceFirstChar(Char::uppercaseChar)
        konst methodName = field.helperMethodName()

        p.println()
        p.println("open fun $methodName(old: $typeName, new: $typeName): Boolean {")

        p.pushIndent()
        p.println("if (old.${fieldName}Count != new.${fieldName}Count) return false")
        p.println()
        p.println("for(i in 0..old.${fieldName}Count - 1) {")
        p.printlnIfWithComparisonIndent(field, "get$capFieldName(i)")
        p.println("}")
        p.println()
        p.println("return true")

        p.popIndent()
        p.println("}")
    }

    abstract inner class FieldGenerator(konst field: Descriptors.FieldDescriptor, konst p: Printer) {
        konst statement = field.getStatement()

        abstract fun Descriptors.FieldDescriptor.getStatement(): String

        fun generate() {
            if (field.isRepeated) {
                printRepeatedField()
            } else if (field.isRequired) {
                printRequiredField()
            } else if (field.isOptional) {
                printOptionalField()
            }

            p.println()

            addMessageTypeToProcessIfNeeded(field)
        }

        abstract fun printRepeatedField()

        abstract fun printRequiredField()

        abstract fun printOptionalField()
    }

    open inner class FieldGeneratorImpl(field: Descriptors.FieldDescriptor, p: Printer) : FieldGenerator(field, p) {
        konst fieldName = field.name.javaName
        konst capFieldName = fieldName.replaceFirstChar(Char::uppercaseChar)

        override fun printRepeatedField() {
            repeatedFields.add(field)
            p.println("if (!${field.helperMethodName()}(old, new)) $statement")
        }

        override fun printRequiredField() {
            p.printlnIfWithComparison(field, fieldName, statement)
        }

        override fun printOptionalField() {
            p.println("if (old.has$capFieldName() != new.has$capFieldName()) $statement")
            p.println("if (old.has$capFieldName()) {")
            p.printlnIfWithComparisonIndent(field, fieldName, statement)
            p.println("}")
        }

        override fun Descriptors.FieldDescriptor.getStatement(): String = "return false"
    }

    open inner class ExtFieldGeneratorImpl(field: Descriptors.FieldDescriptor, p: Printer) : FieldGenerator(field, p) {
        konst outerClassName = field.file.options.javaOuterClassname.removePrefix("Debug")
        konst fieldName = field.name.javaName
        konst fullFieldName = "$outerClassName.$fieldName"

        override fun printRepeatedField() {
            p.printlnMultiline(
                """
                if (old.getExtensionCount($fullFieldName) != new.getExtensionCount($fullFieldName)) {
                    $statement
                }
                else {
                    for(i in 0..old.getExtensionCount($fullFieldName) - 1) {
                        ${ifWithComparison(field, "getExtension($fullFieldName, i)", statement)}
                    }
                }
            """
            )
        }

        override fun printRequiredField() {
            p.printlnIfWithComparison(field, "getExtension($fullFieldName)", statement)
        }

        override fun printOptionalField() {
            p.println("if (old.hasExtension($fullFieldName) != new.hasExtension($fullFieldName)) $statement")
            p.println("if (old.hasExtension($fullFieldName)) {")
            p.printlnIfWithComparisonIndent(field, "getExtension($fullFieldName)", statement)
            p.println("}")
        }

        override fun Descriptors.FieldDescriptor.getStatement(): String = "return false"
    }

    inner class FieldGeneratorForDiff(field: Descriptors.FieldDescriptor, p: Printer) : FieldGeneratorImpl(field, p) {
        override fun Descriptors.FieldDescriptor.getStatement(): String = statementForDiff
    }

    inner class ExtFieldGeneratorForDiff(field: Descriptors.FieldDescriptor, p: Printer) : ExtFieldGeneratorImpl(field, p) {
        override fun Descriptors.FieldDescriptor.getStatement(): String = statementForDiff
    }

    private konst Descriptors.FieldDescriptor.statementForDiff: String
        get() = "$RESULT_NAME.add(${containingType.typeName.replace(".", "")}Kind.${extensions.getEnumName(this)})"

    private konst Descriptors.Descriptor.shouldSkip: Boolean
        get() = options.getExtension(DebugExtOptionsProtoBuf.skipMessageInComparison)

    private konst Descriptors.FieldDescriptor.shouldSkip: Boolean
        get() = options.getExtension(DebugExtOptionsProtoBuf.skipInComparison)
                || (type == Descriptors.FieldDescriptor.Type.MESSAGE && messageType.shouldSkip)

    private fun addMessageTypeToProcessIfNeeded(field: Descriptors.FieldDescriptor) {
        if (field.javaType == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            addMessageToProcessIfNeeded(field.messageType)
        }
    }

    private fun addMessageToProcessIfNeeded(descriptor: Descriptors.Descriptor) {
        if (descriptor !in allMessages && !descriptor.shouldSkip) {
            allMessages.add(descriptor)
            messagesToProcess.add(descriptor)
        }
    }

    private fun ifWithComparison(field: Descriptors.FieldDescriptor, expr: String, statement: String) =
        when {
            field.options.getExtension(DebugExtOptionsProtoBuf.typeIdInTable) ->
                "if (!$CHECK_EQUALS_NAME(oldTypeTable.getType(old.$expr), newTypeTable.getType(new.$expr))) $statement"
            field.options.getExtension(DebugExtOptionsProtoBuf.stringIdInTable) ||
                    field.options.getExtension(DebugExtOptionsProtoBuf.nameIdInTable) ->
                "if (!$CHECK_STRING_EQUALS_NAME(old.$expr, new.$expr)) $statement"
            field.options.getExtension(DebugExtOptionsProtoBuf.fqNameIdInTable) ->
                "if (!$CHECK_CLASS_ID_EQUALS_NAME(old.$expr, new.$expr)) $statement"
            field.javaType in JAVA_TYPES_WITH_INLINED_EQUALS ->
                "if (old.$expr != new.$expr) $statement"
            else ->
                "if (!$CHECK_EQUALS_NAME(old.$expr, new.$expr)) $statement"
        }

    private fun Printer.printlnIfWithComparison(field: Descriptors.FieldDescriptor, expr: String, statement: String = "return false") {
        this.println(ifWithComparison(field, expr, statement))
    }

    fun Printer.printlnIfWithComparisonIndent(field: Descriptors.FieldDescriptor, expr: String, statement: String = "return false") {
        pushIndent()
        printlnIfWithComparison(field, expr, statement)
        popIndent()
    }

    private fun fieldToHashCode(field: Descriptors.FieldDescriptor, expr: String): String =
        when {
            field.options.getExtension(DebugExtOptionsProtoBuf.typeIdInTable) ->
                "typeById($expr).$HASH_CODE_NAME(stringIndexes, fqNameIndexes, typeById)"
            field.options.getExtension(DebugExtOptionsProtoBuf.stringIdInTable) ||
                    field.options.getExtension(DebugExtOptionsProtoBuf.nameIdInTable) ->
                "stringIndexes($expr)"
            field.options.getExtension(DebugExtOptionsProtoBuf.fqNameIdInTable) ->
                "fqNameIndexes($expr)"
            field.javaType == Descriptors.FieldDescriptor.JavaType.INT ->
                "$expr"
            field.javaType in JAVA_TYPES_WITH_INLINED_EQUALS ->
                "$expr.$HASH_CODE_NAME()"
            else ->
                "$expr.$HASH_CODE_NAME(stringIndexes, fqNameIndexes, typeById)"
        }

    private konst Descriptors.Descriptor.typeName: String
        get() {
            konst outerClassName = file.options.javaOuterClassname.removePrefix("Debug")
            konst packageHeader = file.`package`
            return outerClassName + fullName.removePrefix(packageHeader)
        }

    private fun Descriptors.FieldDescriptor.helperMethodName(): String {
        konst packageHeader = this.file.`package`
        konst descriptor = this.containingType
        konst className = descriptor.fullName.removePrefix(packageHeader).replace(".", "")
        konst capFieldName = this.name.javaName.replaceFirstChar(Char::uppercaseChar)
        return "$CHECK_EQUALS_NAME$className$capFieldName"
    }

    private konst String.javaName: String
        get() = this.split("_").joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }.replaceFirstChar(Char::lowercaseChar)
}

private fun Printer.printlnMultiline(string: String) {
    string.trimIndent().split("\n").forEach { this.println(it) }
}
