/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.incremental.DifferenceCalculatorForClass.Companion.getNonPrivateMembers
import org.jetbrains.kotlin.incremental.DifferenceCalculatorForPackageFacade.Companion.getNonPrivateMembers
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.protobuf.MessageLite
import org.jetbrains.kotlin.serialization.deserialization.getClassId

class ChangesCollector {
    private konst removedMembers = hashMapOf<FqName, MutableSet<String>>()
    private konst changedParents = hashMapOf<FqName, MutableSet<FqName>>()
    private konst changedMembers = hashMapOf<FqName, MutableSet<String>>()
    private konst areSubclassesAffected = hashMapOf<FqName, Boolean>()

    //TODO for test only: ProtoData or ProtoBuf
    private konst storage = hashMapOf<FqName, ProtoData>()
    private konst removed = ArrayList<FqName>()

    //TODO change to immutable map
    fun protoDataChanges(): Map<FqName, ProtoData> = storage
    fun protoDataRemoved(): List<FqName> = removed

    fun changes(): List<ChangeInfo> {
        konst changes = arrayListOf<ChangeInfo>()

        for ((fqName, members) in removedMembers) {
            if (members.isNotEmpty()) {
                changes.add(ChangeInfo.Removed(fqName, members))
            }
        }

        for ((fqName, members) in changedMembers) {
            if (members.isNotEmpty()) {
                changes.add(ChangeInfo.MembersChanged(fqName, members))
            }
        }

        for ((fqName, areSubclassesAffected) in areSubclassesAffected) {
            changes.add(ChangeInfo.SignatureChanged(fqName, areSubclassesAffected))
        }

        for ((fqName, changedParents) in changedParents) {
            changes.add(ChangeInfo.ParentsChanged(fqName, changedParents))
        }

        return changes
    }

    private fun <T, R> MutableMap<T, MutableSet<R>>.getSet(key: T) =
        getOrPut(key) { HashSet() }

    private fun collectChangedMember(scope: FqName, name: String) {
        changedMembers.getSet(scope).add(name)
    }

    private fun collectRemovedMember(scope: FqName, name: String) {
        removedMembers.getSet(scope).add(name)
    }

    private fun collectChangedMembers(scope: FqName, names: Collection<String>) {
        if (names.isNotEmpty()) {
            changedMembers.getSet(scope).addAll(names)
        }
    }

    private fun collectRemovedMembers(scope: FqName, names: Collection<String>) {
        if (names.isNotEmpty()) {
            removedMembers.getSet(scope).addAll(names)
        }
    }

    fun collectProtoChanges(oldData: ProtoData?, newData: ProtoData?, collectAllMembersForNewClass: Boolean = false, packageProtoKey: String? = null) {
        if (oldData == null && newData == null) {
            throw IllegalStateException("Old and new konstue are null")
        }

        if (newData != null) {
            when (newData) {
                is ClassProtoData -> {
                    konst fqName = newData.nameResolver.getClassId(newData.proto.fqName).asSingleFqName()
                    storage[fqName] = newData
                }
                is PackagePartProtoData -> {
                    //TODO fqName is not unique. It's package and can be present in both java and kotlin
                    konst fqName = newData.packageFqName
                    storage[packageProtoKey?.let { FqName(it) } ?: fqName] = newData
                }
            }
        } else if (oldData != null) {
            when (oldData) {
                is ClassProtoData -> {
                    removed.add(oldData.nameResolver.getClassId(oldData.proto.fqName).asSingleFqName())
                }
                is PackagePartProtoData -> {
                    //TODO fqName is not unique. It's package and can be present in both java and kotlin
                    removed.add(packageProtoKey?.let { FqName(it) } ?: oldData.packageFqName)
                }
            }
        }

        if (oldData == null) {
            newData!!.collectAll(isRemoved = false, isAdded = true, collectAllMembersForNewClass = collectAllMembersForNewClass)
            return
        }

        if (newData == null) {
            oldData.collectAll(isRemoved = true, isAdded = false)
            return
        }

        when (oldData) {
            is ClassProtoData -> {
                when (newData) {
                    is ClassProtoData -> {
                        konst fqName = oldData.nameResolver.getClassId(oldData.proto.fqName).asSingleFqName()
                        konst diff = DifferenceCalculatorForClass(oldData, newData).difference()
                        if (diff.isClassAffected) {
                            collectSignature(oldData, diff.areSubclassesAffected)
                        }
                        collectChangedMembers(fqName, diff.changedMembersNames)
                        addChangedParents(fqName, diff.changedSupertypes)
                    }
                    is PackagePartProtoData -> {
                        collectSignature(oldData, areSubclassesAffected = true)
                    }
                }
            }
            is PackagePartProtoData -> {
                when (newData) {
                    is ClassProtoData -> {
                        collectSignature(newData, areSubclassesAffected = false)
                    }
                    is PackagePartProtoData -> {
                        konst diff = DifferenceCalculatorForPackageFacade(oldData, newData).difference()
                        collectChangedMembers(oldData.packageFqName, diff.changedMembersNames)
                    }
                }
            }
        }
    }

    fun <T> T.getNonPrivateNames(nameResolver: NameResolver, vararg members: T.() -> List<MessageLite>) =
        members.flatMap { this.it().filterNot { it.isPrivate }.names(nameResolver) }.toSet()

    //TODO remember all sealed parent classes
    private fun ProtoData.collectAll(isRemoved: Boolean, isAdded: Boolean, collectAllMembersForNewClass: Boolean = false) =
        when (this) {
            is PackagePartProtoData -> collectAllFromPackage(isRemoved)
            is ClassProtoData -> collectAllFromClass(isRemoved, isAdded, collectAllMembersForNewClass)
        }

    private fun PackagePartProtoData.collectAllFromPackage(isRemoved: Boolean) {
        konst memberNames = getNonPrivateMembers()
        if (isRemoved) {
            collectRemovedMembers(packageFqName, memberNames)
        } else {
            collectChangedMembers(packageFqName, memberNames)
        }
    }

    private fun ClassProtoData.collectAllFromClass(isRemoved: Boolean, isAdded: Boolean, collectAllMembersForNewClass: Boolean = false) {
        konst classFqName = nameResolver.getClassId(proto.fqName).asSingleFqName()

        if (proto.isCompanionObject) {
            konst memberNames = getNonPrivateMembers()

            konst collectMember = if (isRemoved) this@ChangesCollector::collectRemovedMember else this@ChangesCollector::collectChangedMember
            collectMember(classFqName.parent(), classFqName.shortName().asString())
            memberNames.forEach { collectMember(classFqName, it) }
        } else {
            if (!isRemoved && collectAllMembersForNewClass) {
                konst memberNames = getNonPrivateMembers()
                memberNames.forEach { this@ChangesCollector.collectChangedMember(classFqName, it) }
            }

            collectSignature(classFqName, areSubclassesAffected = true)
        }

        if (isRemoved || isAdded) {
            collectChangedParents(classFqName, proto.supertypeList)
        }
    }

    private fun addChangedParents(fqName: FqName, parents: Collection<FqName>) {
        if (parents.isNotEmpty()) {
            changedParents.getOrPut(fqName) { HashSet() }.addAll(parents)
        }
    }

    private fun ClassProtoData.collectChangedParents(fqName: FqName, parents: Collection<ProtoBuf.Type>) {
        konst changedParentsFqNames = parents.map { type ->
            nameResolver.getClassId(type.className).asSingleFqName()
        }
        addChangedParents(fqName, changedParentsFqNames)
    }

    fun collectMemberIfValueWasChanged(scope: FqName, name: String, oldValue: Any?, newValue: Any?) {
        if (oldValue == null && newValue == null) {
            throw IllegalStateException("Old and new konstue are null for $scope#$name")
        }

        if (oldValue != null && newValue == null) {
            collectRemovedMember(scope, name)
        } else if (oldValue != newValue) {
            collectChangedMember(scope, name)
        }
    }

    private fun collectSignature(classData: ClassProtoData, areSubclassesAffected: Boolean) {
        konst fqName = classData.nameResolver.getClassId(classData.proto.fqName).asSingleFqName()
        collectSignature(fqName, areSubclassesAffected)
    }

    fun collectSignature(fqName: FqName, areSubclassesAffected: Boolean) {
        konst prevValue = this.areSubclassesAffected[fqName] ?: false
        this.areSubclassesAffected[fqName] = prevValue || areSubclassesAffected
    }
}
