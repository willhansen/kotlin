/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.w3c.dom.pointerevents

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*

public external interface PointerEventInit : MouseEventInit, JsAny {
    var pointerId: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var width: Double? /* = 1.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var height: Double? /* = 1.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var pressure: Float? /* = 0f */
        get() = definedExternally
        set(konstue) = definedExternally
    var tangentialPressure: Float? /* = 0f */
        get() = definedExternally
        set(konstue) = definedExternally
    var tiltX: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var tiltY: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var twist: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var pointerType: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var isPrimary: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun PointerEventInit(pointerId: Int? = 0, width: Double? = 1.0, height: Double? = 1.0, pressure: Float? = 0f, tangentialPressure: Float? = 0f, tiltX: Int? = 0, tiltY: Int? = 0, twist: Int? = 0, pointerType: String? = "", isPrimary: Boolean? = false, screenX: Int? = 0, screenY: Int? = 0, clientX: Int? = 0, clientY: Int? = 0, button: Short? = 0, buttons: Short? = 0, relatedTarget: EventTarget? = null, region: String? = null, ctrlKey: Boolean? = false, shiftKey: Boolean? = false, altKey: Boolean? = false, metaKey: Boolean? = false, modifierAltGraph: Boolean? = false, modifierCapsLock: Boolean? = false, modifierFn: Boolean? = false, modifierFnLock: Boolean? = false, modifierHyper: Boolean? = false, modifierNumLock: Boolean? = false, modifierScrollLock: Boolean? = false, modifierSuper: Boolean? = false, modifierSymbol: Boolean? = false, modifierSymbolLock: Boolean? = false, view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): PointerEventInit { js("return { pointerId, width, height, pressure, tangentialPressure, tiltX, tiltY, twist, pointerType, isPrimary, screenX, screenY, clientX, clientY, button, buttons, relatedTarget, region, ctrlKey, shiftKey, altKey, metaKey, modifierAltGraph, modifierCapsLock, modifierFn, modifierFnLock, modifierHyper, modifierNumLock, modifierScrollLock, modifierSuper, modifierSymbol, modifierSymbolLock, view, detail, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [PointerEvent](https://developer.mozilla.org/en/docs/Web/API/PointerEvent) to Kotlin
 */
public external open class PointerEvent(type: String, eventInitDict: PointerEventInit = definedExternally) : MouseEvent, JsAny {
    open konst pointerId: Int
    open konst width: Double
    open konst height: Double
    open konst pressure: Float
    open konst tangentialPressure: Float
    open konst tiltX: Int
    open konst tiltY: Int
    open konst twist: Int
    open konst pointerType: String
    open konst isPrimary: Boolean

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}