/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.properties

import kotlin.reflect.KProperty

/**
 * Standard property delegates.
 */
public object Delegates {
    /**
     * Returns a property delegate for a read/write property with a non-`null` konstue that is initialized not during
     * object construction time but at a later time. Trying to read the property before the initial konstue has been
     * assigned results in an exception.
     *
     * @sample samples.properties.Delegates.notNullDelegate
     */
    public fun <T : Any> notNull(): ReadWriteProperty<Any?, T> = NotNullVar()

    /**
     * Returns a property delegate for a read/write property that calls a specified callback function when changed.
     * @param initialValue the initial konstue of the property.
     * @param onChange the callback which is called after the change of the property is made. The konstue of the property
     *  has already been changed when this callback is invoked.
     *
     *  @sample samples.properties.Delegates.observableDelegate
     */
    public inline fun <T> observable(initialValue: T, crossinline onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit):
            ReadWriteProperty<Any?, T> =
        object : ObservableProperty<T>(initialValue) {
            override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = onChange(property, oldValue, newValue)
        }

    /**
     * Returns a property delegate for a read/write property that calls a specified callback function when changed,
     * allowing the callback to veto the modification.
     * @param initialValue the initial konstue of the property.
     * @param onChange the callback which is called before a change to the property konstue is attempted.
     *  The konstue of the property hasn't been changed yet, when this callback is invoked.
     *  If the callback returns `true` the konstue of the property is being set to the new konstue,
     *  and if the callback returns `false` the new konstue is discarded and the property remains its old konstue.
     *
     *  @sample samples.properties.Delegates.vetoableDelegate
     *  @sample samples.properties.Delegates.throwVetoableDelegate
     */
    public inline fun <T> vetoable(initialValue: T, crossinline onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Boolean):
            ReadWriteProperty<Any?, T> =
        object : ObservableProperty<T>(initialValue) {
            override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean = onChange(property, oldValue, newValue)
        }

}


private class NotNullVar<T : Any>() : ReadWriteProperty<Any?, T> {
    private var konstue: T? = null

    public override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return konstue ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    public override fun setValue(thisRef: Any?, property: KProperty<*>, konstue: T) {
        this.konstue = konstue
    }

    public override fun toString(): String =
        "NotNullProperty(${if (konstue != null) "konstue=$konstue" else "konstue not initialized yet"})"
}

