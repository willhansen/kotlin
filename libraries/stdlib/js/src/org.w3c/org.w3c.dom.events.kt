/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.w3c.dom.events

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*

/**
 * Exposes the JavaScript [UIEvent](https://developer.mozilla.org/en/docs/Web/API/UIEvent) to Kotlin
 */
public external open class UIEvent(type: String, eventInitDict: UIEventInit = definedExternally) : Event {
    open konst view: Window?
    open konst detail: Int

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface UIEventInit : EventInit {
    var view: Window? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var detail: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun UIEventInit(view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): UIEventInit {
    konst o = js("({})")
    o["view"] = view
    o["detail"] = detail
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

/**
 * Exposes the JavaScript [FocusEvent](https://developer.mozilla.org/en/docs/Web/API/FocusEvent) to Kotlin
 */
public external open class FocusEvent(type: String, eventInitDict: FocusEventInit = definedExternally) : UIEvent {
    open konst relatedTarget: EventTarget?

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface FocusEventInit : UIEventInit {
    var relatedTarget: EventTarget? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun FocusEventInit(relatedTarget: EventTarget? = null, view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): FocusEventInit {
    konst o = js("({})")
    o["relatedTarget"] = relatedTarget
    o["view"] = view
    o["detail"] = detail
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

/**
 * Exposes the JavaScript [MouseEvent](https://developer.mozilla.org/en/docs/Web/API/MouseEvent) to Kotlin
 */
public external open class MouseEvent(type: String, eventInitDict: MouseEventInit = definedExternally) : UIEvent, UnionElementOrMouseEvent {
    open konst screenX: Int
    open konst screenY: Int
    open konst clientX: Int
    open konst clientY: Int
    open konst ctrlKey: Boolean
    open konst shiftKey: Boolean
    open konst altKey: Boolean
    open konst metaKey: Boolean
    open konst button: Short
    open konst buttons: Short
    open konst relatedTarget: EventTarget?
    open konst region: String?
    open konst pageX: Double
    open konst pageY: Double
    open konst x: Double
    open konst y: Double
    open konst offsetX: Double
    open konst offsetY: Double
    fun getModifierState(keyArg: String): Boolean

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface MouseEventInit : EventModifierInit {
    var screenX: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var screenY: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var clientX: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var clientY: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var button: Short? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var buttons: Short? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var relatedTarget: EventTarget? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var region: String? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun MouseEventInit(screenX: Int? = 0, screenY: Int? = 0, clientX: Int? = 0, clientY: Int? = 0, button: Short? = 0, buttons: Short? = 0, relatedTarget: EventTarget? = null, region: String? = null, ctrlKey: Boolean? = false, shiftKey: Boolean? = false, altKey: Boolean? = false, metaKey: Boolean? = false, modifierAltGraph: Boolean? = false, modifierCapsLock: Boolean? = false, modifierFn: Boolean? = false, modifierFnLock: Boolean? = false, modifierHyper: Boolean? = false, modifierNumLock: Boolean? = false, modifierScrollLock: Boolean? = false, modifierSuper: Boolean? = false, modifierSymbol: Boolean? = false, modifierSymbolLock: Boolean? = false, view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): MouseEventInit {
    konst o = js("({})")
    o["screenX"] = screenX
    o["screenY"] = screenY
    o["clientX"] = clientX
    o["clientY"] = clientY
    o["button"] = button
    o["buttons"] = buttons
    o["relatedTarget"] = relatedTarget
    o["region"] = region
    o["ctrlKey"] = ctrlKey
    o["shiftKey"] = shiftKey
    o["altKey"] = altKey
    o["metaKey"] = metaKey
    o["modifierAltGraph"] = modifierAltGraph
    o["modifierCapsLock"] = modifierCapsLock
    o["modifierFn"] = modifierFn
    o["modifierFnLock"] = modifierFnLock
    o["modifierHyper"] = modifierHyper
    o["modifierNumLock"] = modifierNumLock
    o["modifierScrollLock"] = modifierScrollLock
    o["modifierSuper"] = modifierSuper
    o["modifierSymbol"] = modifierSymbol
    o["modifierSymbolLock"] = modifierSymbolLock
    o["view"] = view
    o["detail"] = detail
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

public external interface EventModifierInit : UIEventInit {
    var ctrlKey: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var shiftKey: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var altKey: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var metaKey: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierAltGraph: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierCapsLock: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierFn: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierFnLock: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierHyper: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierNumLock: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierScrollLock: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierSuper: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierSymbol: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var modifierSymbolLock: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun EventModifierInit(ctrlKey: Boolean? = false, shiftKey: Boolean? = false, altKey: Boolean? = false, metaKey: Boolean? = false, modifierAltGraph: Boolean? = false, modifierCapsLock: Boolean? = false, modifierFn: Boolean? = false, modifierFnLock: Boolean? = false, modifierHyper: Boolean? = false, modifierNumLock: Boolean? = false, modifierScrollLock: Boolean? = false, modifierSuper: Boolean? = false, modifierSymbol: Boolean? = false, modifierSymbolLock: Boolean? = false, view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): EventModifierInit {
    konst o = js("({})")
    o["ctrlKey"] = ctrlKey
    o["shiftKey"] = shiftKey
    o["altKey"] = altKey
    o["metaKey"] = metaKey
    o["modifierAltGraph"] = modifierAltGraph
    o["modifierCapsLock"] = modifierCapsLock
    o["modifierFn"] = modifierFn
    o["modifierFnLock"] = modifierFnLock
    o["modifierHyper"] = modifierHyper
    o["modifierNumLock"] = modifierNumLock
    o["modifierScrollLock"] = modifierScrollLock
    o["modifierSuper"] = modifierSuper
    o["modifierSymbol"] = modifierSymbol
    o["modifierSymbolLock"] = modifierSymbolLock
    o["view"] = view
    o["detail"] = detail
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

/**
 * Exposes the JavaScript [WheelEvent](https://developer.mozilla.org/en/docs/Web/API/WheelEvent) to Kotlin
 */
public external open class WheelEvent(type: String, eventInitDict: WheelEventInit = definedExternally) : MouseEvent {
    open konst deltaX: Double
    open konst deltaY: Double
    open konst deltaZ: Double
    open konst deltaMode: Int

    companion object {
        konst DOM_DELTA_PIXEL: Int
        konst DOM_DELTA_LINE: Int
        konst DOM_DELTA_PAGE: Int
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface WheelEventInit : MouseEventInit {
    var deltaX: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var deltaY: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var deltaZ: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var deltaMode: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun WheelEventInit(deltaX: Double? = 0.0, deltaY: Double? = 0.0, deltaZ: Double? = 0.0, deltaMode: Int? = 0, screenX: Int? = 0, screenY: Int? = 0, clientX: Int? = 0, clientY: Int? = 0, button: Short? = 0, buttons: Short? = 0, relatedTarget: EventTarget? = null, region: String? = null, ctrlKey: Boolean? = false, shiftKey: Boolean? = false, altKey: Boolean? = false, metaKey: Boolean? = false, modifierAltGraph: Boolean? = false, modifierCapsLock: Boolean? = false, modifierFn: Boolean? = false, modifierFnLock: Boolean? = false, modifierHyper: Boolean? = false, modifierNumLock: Boolean? = false, modifierScrollLock: Boolean? = false, modifierSuper: Boolean? = false, modifierSymbol: Boolean? = false, modifierSymbolLock: Boolean? = false, view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): WheelEventInit {
    konst o = js("({})")
    o["deltaX"] = deltaX
    o["deltaY"] = deltaY
    o["deltaZ"] = deltaZ
    o["deltaMode"] = deltaMode
    o["screenX"] = screenX
    o["screenY"] = screenY
    o["clientX"] = clientX
    o["clientY"] = clientY
    o["button"] = button
    o["buttons"] = buttons
    o["relatedTarget"] = relatedTarget
    o["region"] = region
    o["ctrlKey"] = ctrlKey
    o["shiftKey"] = shiftKey
    o["altKey"] = altKey
    o["metaKey"] = metaKey
    o["modifierAltGraph"] = modifierAltGraph
    o["modifierCapsLock"] = modifierCapsLock
    o["modifierFn"] = modifierFn
    o["modifierFnLock"] = modifierFnLock
    o["modifierHyper"] = modifierHyper
    o["modifierNumLock"] = modifierNumLock
    o["modifierScrollLock"] = modifierScrollLock
    o["modifierSuper"] = modifierSuper
    o["modifierSymbol"] = modifierSymbol
    o["modifierSymbolLock"] = modifierSymbolLock
    o["view"] = view
    o["detail"] = detail
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

/**
 * Exposes the JavaScript [InputEvent](https://developer.mozilla.org/en/docs/Web/API/InputEvent) to Kotlin
 */
public external open class InputEvent(type: String, eventInitDict: InputEventInit = definedExternally) : UIEvent {
    open konst data: String
    open konst isComposing: Boolean

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface InputEventInit : UIEventInit {
    var data: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var isComposing: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun InputEventInit(data: String? = "", isComposing: Boolean? = false, view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): InputEventInit {
    konst o = js("({})")
    o["data"] = data
    o["isComposing"] = isComposing
    o["view"] = view
    o["detail"] = detail
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

/**
 * Exposes the JavaScript [KeyboardEvent](https://developer.mozilla.org/en/docs/Web/API/KeyboardEvent) to Kotlin
 */
public external open class KeyboardEvent(type: String, eventInitDict: KeyboardEventInit = definedExternally) : UIEvent {
    open konst key: String
    open konst code: String
    open konst location: Int
    open konst ctrlKey: Boolean
    open konst shiftKey: Boolean
    open konst altKey: Boolean
    open konst metaKey: Boolean
    open konst repeat: Boolean
    open konst isComposing: Boolean
    open konst charCode: Int
    open konst keyCode: Int
    open konst which: Int
    fun getModifierState(keyArg: String): Boolean

    companion object {
        konst DOM_KEY_LOCATION_STANDARD: Int
        konst DOM_KEY_LOCATION_LEFT: Int
        konst DOM_KEY_LOCATION_RIGHT: Int
        konst DOM_KEY_LOCATION_NUMPAD: Int
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface KeyboardEventInit : EventModifierInit {
    var key: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var code: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var location: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var repeat: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var isComposing: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun KeyboardEventInit(key: String? = "", code: String? = "", location: Int? = 0, repeat: Boolean? = false, isComposing: Boolean? = false, ctrlKey: Boolean? = false, shiftKey: Boolean? = false, altKey: Boolean? = false, metaKey: Boolean? = false, modifierAltGraph: Boolean? = false, modifierCapsLock: Boolean? = false, modifierFn: Boolean? = false, modifierFnLock: Boolean? = false, modifierHyper: Boolean? = false, modifierNumLock: Boolean? = false, modifierScrollLock: Boolean? = false, modifierSuper: Boolean? = false, modifierSymbol: Boolean? = false, modifierSymbolLock: Boolean? = false, view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): KeyboardEventInit {
    konst o = js("({})")
    o["key"] = key
    o["code"] = code
    o["location"] = location
    o["repeat"] = repeat
    o["isComposing"] = isComposing
    o["ctrlKey"] = ctrlKey
    o["shiftKey"] = shiftKey
    o["altKey"] = altKey
    o["metaKey"] = metaKey
    o["modifierAltGraph"] = modifierAltGraph
    o["modifierCapsLock"] = modifierCapsLock
    o["modifierFn"] = modifierFn
    o["modifierFnLock"] = modifierFnLock
    o["modifierHyper"] = modifierHyper
    o["modifierNumLock"] = modifierNumLock
    o["modifierScrollLock"] = modifierScrollLock
    o["modifierSuper"] = modifierSuper
    o["modifierSymbol"] = modifierSymbol
    o["modifierSymbolLock"] = modifierSymbolLock
    o["view"] = view
    o["detail"] = detail
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

/**
 * Exposes the JavaScript [CompositionEvent](https://developer.mozilla.org/en/docs/Web/API/CompositionEvent) to Kotlin
 */
public external open class CompositionEvent(type: String, eventInitDict: CompositionEventInit = definedExternally) : UIEvent {
    open konst data: String

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface CompositionEventInit : UIEventInit {
    var data: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun CompositionEventInit(data: String? = "", view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): CompositionEventInit {
    konst o = js("({})")
    o["data"] = data
    o["view"] = view
    o["detail"] = detail
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

/**
 * Exposes the JavaScript [Event](https://developer.mozilla.org/en/docs/Web/API/Event) to Kotlin
 */
public external open class Event(type: String, eventInitDict: EventInit = definedExternally) {
    open konst type: String
    open konst target: EventTarget?
    open konst currentTarget: EventTarget?
    open konst eventPhase: Short
    open konst bubbles: Boolean
    open konst cancelable: Boolean
    open konst defaultPrevented: Boolean
    open konst composed: Boolean
    open konst isTrusted: Boolean
    open konst timeStamp: Number
    fun composedPath(): Array<EventTarget>
    fun stopPropagation()
    fun stopImmediatePropagation()
    fun preventDefault()
    fun initEvent(type: String, bubbles: Boolean, cancelable: Boolean)

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

/**
 * Exposes the JavaScript [EventTarget](https://developer.mozilla.org/en/docs/Web/API/EventTarget) to Kotlin
 */
public external abstract class EventTarget {
    fun addEventListener(type: String, callback: EventListener?, options: dynamic = definedExternally)
    fun addEventListener(type: String, callback: ((Event) -> Unit)?, options: dynamic = definedExternally)
    fun removeEventListener(type: String, callback: EventListener?, options: dynamic = definedExternally)
    fun removeEventListener(type: String, callback: ((Event) -> Unit)?, options: dynamic = definedExternally)
    fun dispatchEvent(event: Event): Boolean
}

/**
 * Exposes the JavaScript [EventListener](https://developer.mozilla.org/en/docs/Web/API/EventListener) to Kotlin
 */
public external interface EventListener {
    fun handleEvent(event: Event)
}