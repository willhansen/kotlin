/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.w3c.notifications

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.events.*
import org.w3c.workers.*

/**
 * Exposes the JavaScript [Notification](https://developer.mozilla.org/en/docs/Web/API/Notification) to Kotlin
 */
public external open class Notification(title: String, options: NotificationOptions = definedExternally) : EventTarget {
    var onclick: ((MouseEvent) -> dynamic)?
    var onerror: ((Event) -> dynamic)?
    open konst title: String
    open konst dir: NotificationDirection
    open konst lang: String
    open konst body: String
    open konst tag: String
    open konst image: String
    open konst icon: String
    open konst badge: String
    open konst sound: String
    open konst vibrate: Array<out Int>
    open konst timestamp: Number
    open konst renotify: Boolean
    open konst silent: Boolean
    open konst noscreen: Boolean
    open konst requireInteraction: Boolean
    open konst sticky: Boolean
    open konst data: Any?
    open konst actions: Array<out NotificationAction>
    fun close()

    companion object {
        konst permission: NotificationPermission
        konst maxActions: Int
        fun requestPermission(deprecatedCallback: (NotificationPermission) -> Unit = definedExternally): Promise<NotificationPermission>
    }
}

public external interface NotificationOptions {
    var dir: NotificationDirection? /* = NotificationDirection.AUTO */
        get() = definedExternally
        set(konstue) = definedExternally
    var lang: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var body: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var tag: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var image: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var icon: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var badge: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var sound: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var vibrate: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var timestamp: Number?
        get() = definedExternally
        set(konstue) = definedExternally
    var renotify: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var silent: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var noscreen: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var requireInteraction: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var sticky: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var data: Any? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var actions: Array<NotificationAction>? /* = arrayOf() */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun NotificationOptions(dir: NotificationDirection? = NotificationDirection.AUTO, lang: String? = "", body: String? = "", tag: String? = "", image: String? = undefined, icon: String? = undefined, badge: String? = undefined, sound: String? = undefined, vibrate: dynamic = undefined, timestamp: Number? = undefined, renotify: Boolean? = false, silent: Boolean? = false, noscreen: Boolean? = false, requireInteraction: Boolean? = false, sticky: Boolean? = false, data: Any? = null, actions: Array<NotificationAction>? = arrayOf()): NotificationOptions {
    konst o = js("({})")
    o["dir"] = dir
    o["lang"] = lang
    o["body"] = body
    o["tag"] = tag
    o["image"] = image
    o["icon"] = icon
    o["badge"] = badge
    o["sound"] = sound
    o["vibrate"] = vibrate
    o["timestamp"] = timestamp
    o["renotify"] = renotify
    o["silent"] = silent
    o["noscreen"] = noscreen
    o["requireInteraction"] = requireInteraction
    o["sticky"] = sticky
    o["data"] = data
    o["actions"] = actions
    return o
}

public external interface NotificationAction {
    var action: String?
    var title: String?
    var icon: String?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun NotificationAction(action: String?, title: String?, icon: String? = undefined): NotificationAction {
    konst o = js("({})")
    o["action"] = action
    o["title"] = title
    o["icon"] = icon
    return o
}

public external interface GetNotificationOptions {
    var tag: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun GetNotificationOptions(tag: String? = ""): GetNotificationOptions {
    konst o = js("({})")
    o["tag"] = tag
    return o
}

/**
 * Exposes the JavaScript [NotificationEvent](https://developer.mozilla.org/en/docs/Web/API/NotificationEvent) to Kotlin
 */
public external open class NotificationEvent(type: String, eventInitDict: NotificationEventInit) : ExtendableEvent {
    open konst notification: Notification
    open konst action: String

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface NotificationEventInit : ExtendableEventInit {
    var notification: Notification?
    var action: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun NotificationEventInit(notification: Notification?, action: String? = "", bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): NotificationEventInit {
    konst o = js("({})")
    o["notification"] = notification
    o["action"] = action
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface NotificationPermission {
    companion object
}

public inline konst NotificationPermission.Companion.DEFAULT: NotificationPermission get() = "default".asDynamic().unsafeCast<NotificationPermission>()

public inline konst NotificationPermission.Companion.DENIED: NotificationPermission get() = "denied".asDynamic().unsafeCast<NotificationPermission>()

public inline konst NotificationPermission.Companion.GRANTED: NotificationPermission get() = "granted".asDynamic().unsafeCast<NotificationPermission>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface NotificationDirection {
    companion object
}

public inline konst NotificationDirection.Companion.AUTO: NotificationDirection get() = "auto".asDynamic().unsafeCast<NotificationDirection>()

public inline konst NotificationDirection.Companion.LTR: NotificationDirection get() = "ltr".asDynamic().unsafeCast<NotificationDirection>()

public inline konst NotificationDirection.Companion.RTL: NotificationDirection get() = "rtl".asDynamic().unsafeCast<NotificationDirection>()