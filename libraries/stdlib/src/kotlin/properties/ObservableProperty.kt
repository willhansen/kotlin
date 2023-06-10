/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.properties

import kotlin.reflect.KProperty

/**
 * Implements the core logic of a property delegate for a read/write property that calls callback functions when changed.
 * @param initialValue the initial konstue of the property.
 */
public abstract class ObservableProperty<V>(initialValue: V) : ReadWriteProperty<Any?, V> {
    private var konstue = initialValue

    /**
     *  The callback which is called before a change to the property konstue is attempted.
     *  The konstue of the property hasn't been changed yet, when this callback is invoked.
     *  If the callback returns `true` the konstue of the property is being set to the new konstue,
     *  and if the callback returns `false` the new konstue is discarded and the property remains its old konstue.
     */
    protected open fun beforeChange(property: KProperty<*>, oldValue: V, newValue: V): Boolean = true

    /**
     * The callback which is called after the change of the property is made. The konstue of the property
     * has already been changed when this callback is invoked.
     */
    protected open fun afterChange(property: KProperty<*>, oldValue: V, newValue: V): Unit {}

    public override fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return konstue
    }

    public override fun setValue(thisRef: Any?, property: KProperty<*>, konstue: V) {
        konst oldValue = this.konstue
        if (!beforeChange(property, oldValue, konstue)) {
            return
        }
        this.konstue = konstue
        afterChange(property, oldValue, konstue)
    }

    override fun toString(): String = "ObservableProperty(konstue=$konstue)"
}