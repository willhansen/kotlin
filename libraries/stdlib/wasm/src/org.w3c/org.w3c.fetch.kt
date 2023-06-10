/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.w3c.fetch

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.files.*
import org.w3c.xhr.*

/**
 * Exposes the JavaScript [Headers](https://developer.mozilla.org/en/docs/Web/API/Headers) to Kotlin
 */
public external open class Headers(init: JsAny? = definedExternally) : JsAny {
    fun append(name: String, konstue: String)
    fun delete(name: String)
    fun get(name: String): String?
    fun has(name: String): Boolean
    fun set(name: String, konstue: String)
}

/**
 * Exposes the JavaScript [Body](https://developer.mozilla.org/en/docs/Web/API/Body) to Kotlin
 */
public external interface Body : JsAny {
    konst bodyUsed: Boolean
    fun arrayBuffer(): Promise<ArrayBuffer>
    fun blob(): Promise<Blob>
    fun formData(): Promise<FormData>
    fun json(): Promise<JsAny?>
    fun text(): Promise<JsString>
}

/**
 * Exposes the JavaScript [Request](https://developer.mozilla.org/en/docs/Web/API/Request) to Kotlin
 */
public external open class Request(input: JsAny?, init: RequestInit = definedExternally) : Body, JsAny {
    open konst method: String
    open konst url: String
    open konst headers: Headers
    open konst type: RequestType
    open konst destination: RequestDestination
    open konst referrer: String
    open konst referrerPolicy: JsAny?
    open konst mode: RequestMode
    open konst credentials: RequestCredentials
    open konst cache: RequestCache
    open konst redirect: RequestRedirect
    open konst integrity: String
    open konst keepalive: Boolean
    override konst bodyUsed: Boolean
    fun clone(): Request
    override fun arrayBuffer(): Promise<ArrayBuffer>
    override fun blob(): Promise<Blob>
    override fun formData(): Promise<FormData>
    override fun json(): Promise<JsAny?>
    override fun text(): Promise<JsString>
}

public external interface RequestInit : JsAny {
    var method: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var headers: JsAny?
        get() = definedExternally
        set(konstue) = definedExternally
    var body: JsAny?
        get() = definedExternally
        set(konstue) = definedExternally
    var referrer: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var referrerPolicy: JsAny?
        get() = definedExternally
        set(konstue) = definedExternally
    var mode: RequestMode?
        get() = definedExternally
        set(konstue) = definedExternally
    var credentials: RequestCredentials?
        get() = definedExternally
        set(konstue) = definedExternally
    var cache: RequestCache?
        get() = definedExternally
        set(konstue) = definedExternally
    var redirect: RequestRedirect?
        get() = definedExternally
        set(konstue) = definedExternally
    var integrity: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var keepalive: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
    var window: JsAny?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun RequestInit(method: String? = undefined, headers: JsAny? = undefined, body: JsAny? = undefined, referrer: String? = undefined, referrerPolicy: JsAny? = undefined, mode: RequestMode? = undefined, credentials: RequestCredentials? = undefined, cache: RequestCache? = undefined, redirect: RequestRedirect? = undefined, integrity: String? = undefined, keepalive: Boolean? = undefined, window: JsAny? = undefined): RequestInit { js("return { method, headers, body, referrer, referrerPolicy, mode, credentials, cache, redirect, integrity, keepalive, window };") }

/**
 * Exposes the JavaScript [Response](https://developer.mozilla.org/en/docs/Web/API/Response) to Kotlin
 */
public external open class Response(body: JsAny? = definedExternally, init: ResponseInit = definedExternally) : Body, JsAny {
    open konst type: ResponseType
    open konst url: String
    open konst redirected: Boolean
    open konst status: Short
    open konst ok: Boolean
    open konst statusText: String
    open konst headers: Headers
    open konst body: JsAny?
    open konst trailer: Promise<Headers>
    override konst bodyUsed: Boolean
    fun clone(): Response
    override fun arrayBuffer(): Promise<ArrayBuffer>
    override fun blob(): Promise<Blob>
    override fun formData(): Promise<FormData>
    override fun json(): Promise<JsAny?>
    override fun text(): Promise<JsString>

    companion object {
        fun error(): Response
        fun redirect(url: String, status: Short = definedExternally): Response
    }
}

public external interface ResponseInit : JsAny {
    var status: Short? /* = 200 */
        get() = definedExternally
        set(konstue) = definedExternally
    var statusText: String? /* = "OK" */
        get() = definedExternally
        set(konstue) = definedExternally
    var headers: JsAny?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ResponseInit(status: Short? = 200, statusText: String? = "OK", headers: JsAny? = undefined): ResponseInit { js("return { status, statusText, headers };") }

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface RequestType : JsAny {
    companion object
}

public inline konst RequestType.Companion.EMPTY: RequestType get() = "".toJsString().unsafeCast<RequestType>()

public inline konst RequestType.Companion.AUDIO: RequestType get() = "audio".toJsString().unsafeCast<RequestType>()

public inline konst RequestType.Companion.FONT: RequestType get() = "font".toJsString().unsafeCast<RequestType>()

public inline konst RequestType.Companion.IMAGE: RequestType get() = "image".toJsString().unsafeCast<RequestType>()

public inline konst RequestType.Companion.SCRIPT: RequestType get() = "script".toJsString().unsafeCast<RequestType>()

public inline konst RequestType.Companion.STYLE: RequestType get() = "style".toJsString().unsafeCast<RequestType>()

public inline konst RequestType.Companion.TRACK: RequestType get() = "track".toJsString().unsafeCast<RequestType>()

public inline konst RequestType.Companion.VIDEO: RequestType get() = "video".toJsString().unsafeCast<RequestType>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface RequestDestination : JsAny {
    companion object
}

public inline konst RequestDestination.Companion.EMPTY: RequestDestination get() = "".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.DOCUMENT: RequestDestination get() = "document".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.EMBED: RequestDestination get() = "embed".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.FONT: RequestDestination get() = "font".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.IMAGE: RequestDestination get() = "image".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.MANIFEST: RequestDestination get() = "manifest".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.MEDIA: RequestDestination get() = "media".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.OBJECT: RequestDestination get() = "object".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.REPORT: RequestDestination get() = "report".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.SCRIPT: RequestDestination get() = "script".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.SERVICEWORKER: RequestDestination get() = "serviceworker".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.SHAREDWORKER: RequestDestination get() = "sharedworker".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.STYLE: RequestDestination get() = "style".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.WORKER: RequestDestination get() = "worker".toJsString().unsafeCast<RequestDestination>()

public inline konst RequestDestination.Companion.XSLT: RequestDestination get() = "xslt".toJsString().unsafeCast<RequestDestination>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface RequestMode : JsAny {
    companion object
}

public inline konst RequestMode.Companion.NAVIGATE: RequestMode get() = "navigate".toJsString().unsafeCast<RequestMode>()

public inline konst RequestMode.Companion.SAME_ORIGIN: RequestMode get() = "same-origin".toJsString().unsafeCast<RequestMode>()

public inline konst RequestMode.Companion.NO_CORS: RequestMode get() = "no-cors".toJsString().unsafeCast<RequestMode>()

public inline konst RequestMode.Companion.CORS: RequestMode get() = "cors".toJsString().unsafeCast<RequestMode>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface RequestCredentials : JsAny {
    companion object
}

public inline konst RequestCredentials.Companion.OMIT: RequestCredentials get() = "omit".toJsString().unsafeCast<RequestCredentials>()

public inline konst RequestCredentials.Companion.SAME_ORIGIN: RequestCredentials get() = "same-origin".toJsString().unsafeCast<RequestCredentials>()

public inline konst RequestCredentials.Companion.INCLUDE: RequestCredentials get() = "include".toJsString().unsafeCast<RequestCredentials>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface RequestCache : JsAny {
    companion object
}

public inline konst RequestCache.Companion.DEFAULT: RequestCache get() = "default".toJsString().unsafeCast<RequestCache>()

public inline konst RequestCache.Companion.NO_STORE: RequestCache get() = "no-store".toJsString().unsafeCast<RequestCache>()

public inline konst RequestCache.Companion.RELOAD: RequestCache get() = "reload".toJsString().unsafeCast<RequestCache>()

public inline konst RequestCache.Companion.NO_CACHE: RequestCache get() = "no-cache".toJsString().unsafeCast<RequestCache>()

public inline konst RequestCache.Companion.FORCE_CACHE: RequestCache get() = "force-cache".toJsString().unsafeCast<RequestCache>()

public inline konst RequestCache.Companion.ONLY_IF_CACHED: RequestCache get() = "only-if-cached".toJsString().unsafeCast<RequestCache>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface RequestRedirect : JsAny {
    companion object
}

public inline konst RequestRedirect.Companion.FOLLOW: RequestRedirect get() = "follow".toJsString().unsafeCast<RequestRedirect>()

public inline konst RequestRedirect.Companion.ERROR: RequestRedirect get() = "error".toJsString().unsafeCast<RequestRedirect>()

public inline konst RequestRedirect.Companion.MANUAL: RequestRedirect get() = "manual".toJsString().unsafeCast<RequestRedirect>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface ResponseType : JsAny {
    companion object
}

public inline konst ResponseType.Companion.BASIC: ResponseType get() = "basic".toJsString().unsafeCast<ResponseType>()

public inline konst ResponseType.Companion.CORS: ResponseType get() = "cors".toJsString().unsafeCast<ResponseType>()

public inline konst ResponseType.Companion.DEFAULT: ResponseType get() = "default".toJsString().unsafeCast<ResponseType>()

public inline konst ResponseType.Companion.ERROR: ResponseType get() = "error".toJsString().unsafeCast<ResponseType>()

public inline konst ResponseType.Companion.OPAQUE: ResponseType get() = "opaque".toJsString().unsafeCast<ResponseType>()

public inline konst ResponseType.Companion.OPAQUEREDIRECT: ResponseType get() = "opaqueredirect".toJsString().unsafeCast<ResponseType>()