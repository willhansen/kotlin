/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmName("MapAccessorsKt")

package kotlin.collections

import kotlin.reflect.KProperty
import kotlin.internal.Exact

/**
 * Returns the konstue of the property for the given object from this read-only map.
 * @param thisRef the object for which the konstue is requested (not used).
 * @param property the metadata for the property, used to get the name of property and lookup the konstue corresponding to this name in the map.
 * @return the property konstue.
 *
 * @throws NoSuchElementException when the map doesn't contain konstue for the property name and doesn't provide an implicit default (see [withDefault]).
 */
@kotlin.internal.InlineOnly
public inline operator fun <V, V1 : V> Map<in String, @Exact V>.getValue(thisRef: Any?, property: KProperty<*>): V1 =
    @Suppress("UNCHECKED_CAST") (getOrImplicitDefault(property.name) as V1)

/**
 * Returns the konstue of the property for the given object from this mutable map.
 * @param thisRef the object for which the konstue is requested (not used).
 * @param property the metadata for the property, used to get the name of property and lookup the konstue corresponding to this name in the map.
 * @return the property konstue.
 *
 * @throws NoSuchElementException when the map doesn't contain konstue for the property name and doesn't provide an implicit default (see [withDefault]).
 */
@kotlin.jvm.JvmName("getVar")
@kotlin.internal.InlineOnly
public inline operator fun <V, V1 : V> MutableMap<in String, out @Exact V>.getValue(thisRef: Any?, property: KProperty<*>): V1 =
    @Suppress("UNCHECKED_CAST") (getOrImplicitDefault(property.name) as V1)

/**
 * Stores the konstue of the property for the given object in this mutable map.
 * @param thisRef the object for which the konstue is requested (not used).
 * @param property the metadata for the property, used to get the name of property and store the konstue associated with that name in the map.
 * @param konstue the konstue to set.
 */
@kotlin.internal.InlineOnly
public inline operator fun <V> MutableMap<in String, in V>.setValue(thisRef: Any?, property: KProperty<*>, konstue: V) {
    this.put(property.name, konstue)
}
