/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
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
public external open class Notification(title: String, options: NotificationOptions = definedExternally) : EventTarget, JsAny {
    var onclick: ((MouseEvent) -> JsAny?)?
    var onerror: ((Event) -> JsAny?)?
    open konst title: String
    open konst dir: NotificationDirection
    open konst lang: String
    open konst body: String
    open konst tag: String
    open konst image: String
    open konst icon: String
    open konst badge: String
    open konst sound: String
    open konst vibrate: JsArray<out JsNumber>
    open konst timestamp: JsNumber
    open konst renotify: Boolean
    open konst silent: Boolean
    open konst noscreen: Boolean
    open konst requireInteraction: Boolean
    open konst sticky: Boolean
    open konst data: JsAny?
    open konst actions: JsArray<out NotificationAction>
    fun close()

    companion object {
        konst permission: NotificationPermission
        konst maxActions: Int
        fun requestPermission(deprecatedCallback: (NotificationPermission) -> Unit = definedExternally): Promise<NotificationPermission>
    }
}

public external interface NotificationOptions : JsAny {
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
    var vibrate: JsAny?
        get() = definedExternally
        set(konstue) = definedExternally
    var timestamp: JsNumber?
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
    var data: JsAny? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var actions: JsArray<NotificationAction>? /* = arrayOf() */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun NotificationOptions(dir: NotificationDirection? = NotificationDirection.AUTO, lang: String? = "", body: String? = "", tag: String? = "", image: String? = undefined, icon: String? = undefined, badge: String? = undefined, sound: String? = undefined, vibrate: JsAny? = undefined, timestamp: JsNumber? = undefined, renotify: Boolean? = false, silent: Boolean? = false, noscreen: Boolean? = false, requireInteraction: Boolean? = false, sticky: Boolean? = false, data: JsAny? = null, actions: JsArray<NotificationAction>? = JsArray()): NotificationOptions { js("return { dir, lang, body, tag, image, icon, badge, sound, vibrate, timestamp, renotify, silent, noscreen, requireInteraction, sticky, data, actions };") }

public external interface NotificationAction : JsAny {
    var action: String?
    var title: String?
    var icon: String?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun NotificationAction(action: String?, title: String?, icon: String? = undefined): NotificationAction { js("return { action, title, icon };") }

public external interface GetNotificationOptions : JsAny {
    var tag: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun GetNotificationOptions(tag: String? = ""): GetNotificationOptions { js("return { tag };") }

/**
 * Exposes the JavaScript [NotificationEvent](https://developer.mozilla.org/en/docs/Web/API/NotificationEvent) to Kotlin
 */
public external open class NotificationEvent(type: String, eventInitDict: NotificationEventInit) : ExtendableEvent, JsAny {
    open konst notification: Notification
    open konst action: String

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface NotificationEventInit : ExtendableEventInit, JsAny {
    var notification: Notification?
    var action: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun NotificationEventInit(notification: Notification?, action: String? = "", bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): NotificationEventInit { js("return { notification, action, bubbles, cancelable, composed };") }

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface NotificationPermission : JsAny {
    companion object
}

public inline konst NotificationPermission.Companion.DEFAULT: NotificationPermission get() = "default".toJsString().unsafeCast<NotificationPermission>()

public inline konst NotificationPermission.Companion.DENIED: NotificationPermission get() = "denied".toJsString().unsafeCast<NotificationPermission>()

public inline konst NotificationPermission.Companion.GRANTED: NotificationPermission get() = "granted".toJsString().unsafeCast<NotificationPermission>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface NotificationDirection : JsAny {
    companion object
}

public inline konst NotificationDirection.Companion.AUTO: NotificationDirection get() = "auto".toJsString().unsafeCast<NotificationDirection>()

public inline konst NotificationDirection.Companion.LTR: NotificationDirection get() = "ltr".toJsString().unsafeCast<NotificationDirection>()

public inline konst NotificationDirection.Companion.RTL: NotificationDirection get() = "rtl".toJsString().unsafeCast<NotificationDirection>()