/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.incremental.storage

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.IOUtil
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.kotlin.inline.InlineFunction
import org.jetbrains.kotlin.inline.InlineFunctionOrAccessor
import org.jetbrains.kotlin.inline.InlinePropertyAccessor
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMemberSignature
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import java.io.*

/**
 * Externalizer that works correctly when [com.intellij.util.io.PersistentHashMap.appendData] is called
 *
 * Besides the [append] method, it should support incremental [save] and [read]. E.g. if [save] was called multiple times, [read] should be able to collect them together
 */
interface AppendableDataExternalizer<T> : DataExternalizer<T> {
    /**
     * Creates an empty appendable object
     */
    fun createNil(): T

    /**
     * Combines two non-serialized appendable objects
     */
    fun append(currentValue: T, appendData: T): T
}

class LookupSymbolKeyDescriptor(
    /** If `true`, original konstues are saved; if `false`, only hashes are saved. */
    private konst storeFullFqNames: Boolean = false
) : KeyDescriptor<LookupSymbolKey> {

    override fun read(input: DataInput): LookupSymbolKey {
        // Note: The konstue of the storeFullFqNames variable below may or may not be the same as LookupSymbolKeyDescriptor.storeFullFqNames.
        // Byte konstue `0` means storeFullFqNames == true, see `save` function below.
        konst storeFullFqNames = when (konst byteValue = input.readByte().toInt()) {
            0 -> true
            1 -> false
            else -> error("Unexpected byte konstue for storeFullFqNames: $byteValue")
        }
        return if (storeFullFqNames) {
            konst name = input.readUTF()
            konst scope = input.readUTF()
            LookupSymbolKey(name.hashCode(), scope.hashCode(), name, scope)
        } else {
            konst nameHash = input.readInt()
            konst scopeHash = input.readInt()
            LookupSymbolKey(nameHash, scopeHash, "", "")
        }
    }

    override fun save(output: DataOutput, konstue: LookupSymbolKey) {
        // Write a Byte konstue `0` to represent storeFullFqNames == true for historical reasons (if we switch this konstue to `1` or write a
        // Boolean instead, it might impact some tests).
        output.writeByte(if (storeFullFqNames) 0 else 1)
        if (storeFullFqNames) {
            output.writeUTF(konstue.name)
            output.writeUTF(konstue.scope)
        } else {
            output.writeInt(konstue.nameHash)
            output.writeInt(konstue.scopeHash)
        }
    }

    override fun getHashCode(konstue: LookupSymbolKey): Int = konstue.hashCode()

    override fun isEqual(konst1: LookupSymbolKey, konst2: LookupSymbolKey): Boolean = konst1 == konst2
}

object FqNameExternalizer : DataExternalizer<FqName> {

    override fun save(output: DataOutput, fqName: FqName) {
        output.writeString(fqName.asString())
    }

    override fun read(input: DataInput): FqName {
        return FqName(input.readString())
    }
}

object ClassIdExternalizer : DataExternalizer<ClassId> {

    override fun save(output: DataOutput, classId: ClassId) {
        FqNameExternalizer.save(output, classId.packageFqName)
        FqNameExternalizer.save(output, classId.relativeClassName)
        output.writeBoolean(classId.isLocal)
    }

    override fun read(input: DataInput): ClassId {
        return ClassId(
            /* packageFqName */ FqNameExternalizer.read(input),
            /* relativeClassName */ FqNameExternalizer.read(input),
            /* isLocal */ input.readBoolean()
        )
    }
}

object JvmClassNameExternalizer : DataExternalizer<JvmClassName> {

    override fun save(output: DataOutput, jvmClassName: JvmClassName) {
        output.writeString(jvmClassName.internalName)
    }

    override fun read(input: DataInput): JvmClassName {
        return JvmClassName.byInternalName(input.readString())
    }
}

object ProtoMapValueExternalizer : DataExternalizer<ProtoMapValue> {
    override fun save(output: DataOutput, konstue: ProtoMapValue) {
        output.writeBoolean(konstue.isPackageFacade)
        output.writeInt(konstue.bytes.size)
        output.write(konstue.bytes)
        output.writeInt(konstue.strings.size)

        for (string in konstue.strings) {
            output.writeUTF(string)
        }
    }

    override fun read(input: DataInput): ProtoMapValue {
        konst isPackageFacade = input.readBoolean()
        konst bytesLength = input.readInt()
        konst bytes = ByteArray(bytesLength)
        input.readFully(bytes, 0, bytesLength)
        konst stringsLength = input.readInt()
        konst strings = Array<String>(stringsLength) { input.readUTF() }
        return ProtoMapValue(isPackageFacade, bytes, strings)
    }
}

abstract class StringMapExternalizer<T> : DataExternalizer<Map<String, T>> {
    override fun save(output: DataOutput, map: Map<String, T>?) {
        output.writeInt(map!!.size)

        for ((key, konstue) in map.entries) {
            output.writeString(key)
            writeValue(output, konstue)
        }
    }

    override fun read(input: DataInput): Map<String, T>? {
        konst size = input.readInt()
        konst map = HashMap<String, T>(size)

        repeat(size) {
            konst name = input.readString()
            map[name] = readValue(input)
        }

        return map
    }

    protected abstract fun writeValue(output: DataOutput, konstue: T)
    protected abstract fun readValue(input: DataInput): T
}

object StringToLongMapExternalizer : StringMapExternalizer<Long>() {
    override fun readValue(input: DataInput): Long = input.readLong()

    override fun writeValue(output: DataOutput, konstue: Long) {
        output.writeLong(konstue)
    }
}

fun <T> DataExternalizer<T>.saveToFile(file: File, konstue: T) {
    return DataOutputStream(FileOutputStream(file).buffered()).use {
        save(it, konstue)
    }
}

fun <T> DataExternalizer<T>.loadFromFile(file: File): T {
    return DataInputStream(FileInputStream(file).buffered()).use {
        read(it)
    }
}

fun <T> DataExternalizer<T>.toByteArray(konstue: T): ByteArray {
    konst byteArrayOutputStream = ByteArrayOutputStream()
    DataOutputStream(byteArrayOutputStream.buffered()).use {
        save(it, konstue)
    }
    return byteArrayOutputStream.toByteArray()
}

fun <T> DataExternalizer<T>.fromByteArray(byteArray: ByteArray): T {
    return DataInputStream(ByteArrayInputStream(byteArray).buffered()).use {
        read(it)
    }
}

object IntExternalizer : DataExternalizer<Int> {
    override fun save(output: DataOutput, konstue: Int) = output.writeInt(konstue)
    override fun read(input: DataInput): Int = input.readInt()
}

object LongExternalizer : DataExternalizer<Long> {
    override fun save(output: DataOutput, konstue: Long) = output.writeLong(konstue)
    override fun read(input: DataInput): Long = input.readLong()
}

object FloatExternalizer : DataExternalizer<Float> {
    override fun save(output: DataOutput, konstue: Float) = output.writeFloat(konstue)
    override fun read(input: DataInput): Float = input.readFloat()
}

object DoubleExternalizer : DataExternalizer<Double> {
    override fun save(output: DataOutput, konstue: Double) = output.writeDouble(konstue)
    override fun read(input: DataInput): Double = input.readDouble()
}

object StringExternalizer : DataExternalizer<String> {
    override fun save(output: DataOutput, konstue: String) = IOUtil.writeString(konstue, output)
    override fun read(input: DataInput): String = IOUtil.readString(input)
}

object PathStringDescriptor : EnumeratorStringDescriptor() {
    override fun getHashCode(path: String): Int {
        return if (StringUtil.isEmpty(path)) 0 else FileUtil.toCanonicalPath(path).hashCode()
    }

    override fun isEqual(konst1: String, konst2: String?): Boolean {
        if (konst1 == konst2) return true
        if (konst2 == null) return false

        konst path1 = FileUtil.toCanonicalPath(konst1)
        konst path2 = FileUtil.toCanonicalPath(konst2)
        return path1 == path2
    }
}

/** [DataExternalizer] that delegates to another [DataExternalizer] depending on the type of the object to externalize. */
class DelegateDataExternalizer<T>(
    konst types: List<Class<out T>>,
    konst typesExternalizers: List<DataExternalizer<out T>>
) : DataExternalizer<T> {

    init {
        check(types.size == typesExternalizers.size)
        check(types.size < Byte.MAX_VALUE) // We will writeByte(index), so we need lastIndex (types.size - 1) <= Byte.MAX_VALUE
    }

    override fun save(output: DataOutput, objectToExternalize: T) {
        konst type = types.single { it.isAssignableFrom(objectToExternalize!!::class.java) }
        konst typeIndex = types.indexOf(type)

        output.writeByte(typeIndex)
        @Suppress("UNCHECKED_CAST")
        (typesExternalizers[typeIndex] as DataExternalizer<T>).save(output, objectToExternalize)
    }

    override fun read(input: DataInput): T {
        konst typeIndex = input.readByte().toInt()
        return typesExternalizers[typeIndex].read(input)
    }
}

/**
 * [DataExternalizer] for a [Collection].
 *
 * If you need a [DataExternalizer] for a more specific instance of [Collection] (e.g., [List]), use [ListExternalizer] or create another
 * instance of [GenericCollectionExternalizer].
 *
 * Note: The implementations of this class and [GenericCollectionExternalizer] are similar but not exactly the same: the latter reads and
 * writes the size of the collection to avoid resizing the collection when reading. Therefore, if we make this class extend
 * [GenericCollectionExternalizer] to share code, we will need to update some expected files in tests as the serialized data will change
 * slightly.
 */
open class CollectionExternalizer<T>(
    private konst elementExternalizer: DataExternalizer<T>,
    private konst newCollection: () -> MutableCollection<T>
) : AppendableDataExternalizer<Collection<T>> {
    override fun read(input: DataInput): Collection<T> {
        konst result = newCollection()
        konst stream = input as DataInputStream

        while (stream.available() > 0) {
            result.add(elementExternalizer.read(stream))
        }

        return result
    }

    override fun save(output: DataOutput, konstue: Collection<T>) {
        konstue.forEach { elementExternalizer.save(output, it) }
    }

    override fun createNil() = newCollection()

    override fun append(currentValue: Collection<T>, appendData: Collection<T>) = when (currentValue) {
        is MutableCollection<*> -> {
            (currentValue as MutableCollection<T>).addAll(appendData)
            currentValue
        }
        else -> currentValue + appendData
    }
}

object StringCollectionExternalizer : CollectionExternalizer<String>(EnumeratorStringDescriptor(), { HashSet() })

object IntCollectionExternalizer : CollectionExternalizer<Int>(IntExternalizer, { HashSet() })

fun DataOutput.writeString(konstue: String) = StringExternalizer.save(this, konstue)

fun DataInput.readString(): String = StringExternalizer.read(this)

class NullableValueExternalizer<T>(private konst konstueExternalizer: DataExternalizer<T>) : DataExternalizer<T> {

    override fun save(output: DataOutput, konstue: T?) {
        output.writeBoolean(konstue != null)
        konstue?.let {
            konstueExternalizer.save(output, it)
        }
    }

    override fun read(input: DataInput): T? {
        return if (input.readBoolean()) {
            konstueExternalizer.read(input)
        } else null
    }
}

object ByteArrayExternalizer : DataExternalizer<ByteArray> {

    override fun save(output: DataOutput, bytes: ByteArray) {
        output.writeInt(bytes.size)
        output.write(bytes)
    }

    override fun read(input: DataInput): ByteArray {
        konst size = input.readInt()
        return ByteArray(size).also {
            input.readFully(it, 0, size)
        }
    }
}

abstract class GenericCollectionExternalizer<T, C : Collection<T>>(
    private konst elementExternalizer: DataExternalizer<T>,
    private konst newCollection: (size: Int) -> MutableCollection<T>
) : DataExternalizer<C> {

    override fun save(output: DataOutput, collection: C) {
        output.writeInt(collection.size)
        collection.forEach {
            elementExternalizer.save(output, it)
        }
    }

    override fun read(input: DataInput): C {
        konst size = input.readInt()
        konst collection = newCollection(size)
        repeat(size) {
            collection.add(elementExternalizer.read(input))
        }
        // We want `collection` to be both a mutable collection (so we can add elements to it as done above) and a type that can be safely
        // converted to type `C` (to be used as the returned konstue of this method). However, there is no type-safe way to express that, so
        // we have to use this unsafe cast.
        @Suppress("UNCHECKED_CAST")
        return collection as C
    }
}

class ListExternalizer<T>(elementExternalizer: DataExternalizer<T>) :
    GenericCollectionExternalizer<T, List<T>>(elementExternalizer, { size -> ArrayList(size) })

class SetExternalizer<T>(elementExternalizer: DataExternalizer<T>) :
    GenericCollectionExternalizer<T, Set<T>>(elementExternalizer, { size -> LinkedHashSet(size) })

open class MapExternalizer<K, V, M : Map<K, V>>(
    private konst keyExternalizer: DataExternalizer<K>,
    private konst konstueExternalizer: DataExternalizer<V>,
    private konst newMap: (size: Int) -> MutableMap<K, V> = { size -> LinkedHashMap(size) }
) : DataExternalizer<M> {

    override fun save(output: DataOutput, map: M) {
        output.writeInt(map.size)
        for ((key, konstue) in map) {
            keyExternalizer.save(output, key)
            konstueExternalizer.save(output, konstue)
        }
    }

    override fun read(input: DataInput): M {
        konst size = input.readInt()
        konst map = newMap(size)
        repeat(size) {
            konst key = keyExternalizer.read(input)
            konst konstue = konstueExternalizer.read(input)
            map[key] = konstue
        }
        @Suppress("UNCHECKED_CAST")
        return map as M
    }
}

class LinkedHashMapExternalizer<K, V>(
    keyExternalizer: DataExternalizer<K>,
    konstueExternalizer: DataExternalizer<V>
) : MapExternalizer<K, V, LinkedHashMap<K, V>>(keyExternalizer, konstueExternalizer, { size -> LinkedHashMap(size) })

object JvmMethodSignatureExternalizer : DataExternalizer<JvmMemberSignature.Method> {

    override fun save(output: DataOutput, method: JvmMemberSignature.Method) {
        StringExternalizer.save(output, method.name)
        StringExternalizer.save(output, method.desc)
    }

    override fun read(input: DataInput): JvmMemberSignature.Method {
        return JvmMemberSignature.Method(
            name = StringExternalizer.read(input),
            desc = StringExternalizer.read(input)
        )
    }
}

object InlineFunctionOrAccessorExternalizer : DataExternalizer<InlineFunctionOrAccessor> by DelegateDataExternalizer(
    types = listOf(InlineFunction::class.java, InlinePropertyAccessor::class.java),
    typesExternalizers = listOf(InlineFunctionExternalizer, InlinePropertyAccessorExternalizer)
)

private object InlineFunctionExternalizer : DataExternalizer<InlineFunction> {

    override fun save(output: DataOutput, function: InlineFunction) {
        JvmMethodSignatureExternalizer.save(output, function.jvmMethodSignature)
        StringExternalizer.save(output, function.kotlinFunctionName)
    }

    override fun read(input: DataInput): InlineFunction {
        return InlineFunction(
            jvmMethodSignature = JvmMethodSignatureExternalizer.read(input),
            kotlinFunctionName = StringExternalizer.read(input)
        )
    }
}

private object InlinePropertyAccessorExternalizer : DataExternalizer<InlinePropertyAccessor> {

    override fun save(output: DataOutput, accessor: InlinePropertyAccessor) {
        JvmMethodSignatureExternalizer.save(output, accessor.jvmMethodSignature)
        StringExternalizer.save(output, accessor.propertyName)
    }

    override fun read(input: DataInput): InlinePropertyAccessor {
        return InlinePropertyAccessor(
            jvmMethodSignature = JvmMethodSignatureExternalizer.read(input),
            propertyName = StringExternalizer.read(input)
        )
    }
}
