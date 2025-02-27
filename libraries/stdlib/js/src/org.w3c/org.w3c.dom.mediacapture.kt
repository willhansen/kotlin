/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.w3c.dom.mediacapture

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*

/**
 * Exposes the JavaScript [MediaStream](https://developer.mozilla.org/en/docs/Web/API/MediaStream) to Kotlin
 */
public external open class MediaStream() : EventTarget, MediaProvider {
    constructor(stream: MediaStream)
    constructor(tracks: Array<MediaStreamTrack>)
    open konst id: String
    open konst active: Boolean
    var onaddtrack: ((MediaStreamTrackEvent) -> dynamic)?
    var onremovetrack: ((MediaStreamTrackEvent) -> dynamic)?
    fun getAudioTracks(): Array<MediaStreamTrack>
    fun getVideoTracks(): Array<MediaStreamTrack>
    fun getTracks(): Array<MediaStreamTrack>
    fun getTrackById(trackId: String): MediaStreamTrack?
    fun addTrack(track: MediaStreamTrack)
    fun removeTrack(track: MediaStreamTrack)
    fun clone(): MediaStream
}

/**
 * Exposes the JavaScript [MediaStreamTrack](https://developer.mozilla.org/en/docs/Web/API/MediaStreamTrack) to Kotlin
 */
public external abstract class MediaStreamTrack : EventTarget {
    open konst kind: String
    open konst id: String
    open konst label: String
    open var enabled: Boolean
    open konst muted: Boolean
    open var onmute: ((Event) -> dynamic)?
    open var onunmute: ((Event) -> dynamic)?
    open konst readyState: MediaStreamTrackState
    open var onended: ((Event) -> dynamic)?
    open var onoverconstrained: ((Event) -> dynamic)?
    fun clone(): MediaStreamTrack
    fun stop()
    fun getCapabilities(): MediaTrackCapabilities
    fun getConstraints(): MediaTrackConstraints
    fun getSettings(): MediaTrackSettings
    fun applyConstraints(constraints: MediaTrackConstraints = definedExternally): Promise<Unit>
}

/**
 * Exposes the JavaScript [MediaTrackSupportedConstraints](https://developer.mozilla.org/en/docs/Web/API/MediaTrackSupportedConstraints) to Kotlin
 */
public external interface MediaTrackSupportedConstraints {
    var width: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var height: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var aspectRatio: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var frameRate: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var facingMode: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var resizeMode: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var volume: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var sampleRate: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var sampleSize: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var echoCancellation: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var autoGainControl: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var noiseSuppression: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var latency: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var channelCount: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var deviceId: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var groupId: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun MediaTrackSupportedConstraints(width: Boolean? = true, height: Boolean? = true, aspectRatio: Boolean? = true, frameRate: Boolean? = true, facingMode: Boolean? = true, resizeMode: Boolean? = true, volume: Boolean? = true, sampleRate: Boolean? = true, sampleSize: Boolean? = true, echoCancellation: Boolean? = true, autoGainControl: Boolean? = true, noiseSuppression: Boolean? = true, latency: Boolean? = true, channelCount: Boolean? = true, deviceId: Boolean? = true, groupId: Boolean? = true): MediaTrackSupportedConstraints {
    konst o = js("({})")
    o["width"] = width
    o["height"] = height
    o["aspectRatio"] = aspectRatio
    o["frameRate"] = frameRate
    o["facingMode"] = facingMode
    o["resizeMode"] = resizeMode
    o["volume"] = volume
    o["sampleRate"] = sampleRate
    o["sampleSize"] = sampleSize
    o["echoCancellation"] = echoCancellation
    o["autoGainControl"] = autoGainControl
    o["noiseSuppression"] = noiseSuppression
    o["latency"] = latency
    o["channelCount"] = channelCount
    o["deviceId"] = deviceId
    o["groupId"] = groupId
    return o
}

public external interface MediaTrackCapabilities {
    var width: ULongRange?
        get() = definedExternally
        set(konstue) = definedExternally
    var height: ULongRange?
        get() = definedExternally
        set(konstue) = definedExternally
    var aspectRatio: DoubleRange?
        get() = definedExternally
        set(konstue) = definedExternally
    var frameRate: DoubleRange?
        get() = definedExternally
        set(konstue) = definedExternally
    var facingMode: Array<String>?
        get() = definedExternally
        set(konstue) = definedExternally
    var resizeMode: Array<String>?
        get() = definedExternally
        set(konstue) = definedExternally
    var volume: DoubleRange?
        get() = definedExternally
        set(konstue) = definedExternally
    var sampleRate: ULongRange?
        get() = definedExternally
        set(konstue) = definedExternally
    var sampleSize: ULongRange?
        get() = definedExternally
        set(konstue) = definedExternally
    var echoCancellation: Array<Boolean>?
        get() = definedExternally
        set(konstue) = definedExternally
    var autoGainControl: Array<Boolean>?
        get() = definedExternally
        set(konstue) = definedExternally
    var noiseSuppression: Array<Boolean>?
        get() = definedExternally
        set(konstue) = definedExternally
    var latency: DoubleRange?
        get() = definedExternally
        set(konstue) = definedExternally
    var channelCount: ULongRange?
        get() = definedExternally
        set(konstue) = definedExternally
    var deviceId: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var groupId: String?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun MediaTrackCapabilities(width: ULongRange? = undefined, height: ULongRange? = undefined, aspectRatio: DoubleRange? = undefined, frameRate: DoubleRange? = undefined, facingMode: Array<String>? = undefined, resizeMode: Array<String>? = undefined, volume: DoubleRange? = undefined, sampleRate: ULongRange? = undefined, sampleSize: ULongRange? = undefined, echoCancellation: Array<Boolean>? = undefined, autoGainControl: Array<Boolean>? = undefined, noiseSuppression: Array<Boolean>? = undefined, latency: DoubleRange? = undefined, channelCount: ULongRange? = undefined, deviceId: String? = undefined, groupId: String? = undefined): MediaTrackCapabilities {
    konst o = js("({})")
    o["width"] = width
    o["height"] = height
    o["aspectRatio"] = aspectRatio
    o["frameRate"] = frameRate
    o["facingMode"] = facingMode
    o["resizeMode"] = resizeMode
    o["volume"] = volume
    o["sampleRate"] = sampleRate
    o["sampleSize"] = sampleSize
    o["echoCancellation"] = echoCancellation
    o["autoGainControl"] = autoGainControl
    o["noiseSuppression"] = noiseSuppression
    o["latency"] = latency
    o["channelCount"] = channelCount
    o["deviceId"] = deviceId
    o["groupId"] = groupId
    return o
}

/**
 * Exposes the JavaScript [MediaTrackConstraints](https://developer.mozilla.org/en/docs/Web/API/MediaTrackConstraints) to Kotlin
 */
public external interface MediaTrackConstraints : MediaTrackConstraintSet {
    var advanced: Array<MediaTrackConstraintSet>?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun MediaTrackConstraints(advanced: Array<MediaTrackConstraintSet>? = undefined, width: dynamic = undefined, height: dynamic = undefined, aspectRatio: dynamic = undefined, frameRate: dynamic = undefined, facingMode: dynamic = undefined, resizeMode: dynamic = undefined, volume: dynamic = undefined, sampleRate: dynamic = undefined, sampleSize: dynamic = undefined, echoCancellation: dynamic = undefined, autoGainControl: dynamic = undefined, noiseSuppression: dynamic = undefined, latency: dynamic = undefined, channelCount: dynamic = undefined, deviceId: dynamic = undefined, groupId: dynamic = undefined): MediaTrackConstraints {
    konst o = js("({})")
    o["advanced"] = advanced
    o["width"] = width
    o["height"] = height
    o["aspectRatio"] = aspectRatio
    o["frameRate"] = frameRate
    o["facingMode"] = facingMode
    o["resizeMode"] = resizeMode
    o["volume"] = volume
    o["sampleRate"] = sampleRate
    o["sampleSize"] = sampleSize
    o["echoCancellation"] = echoCancellation
    o["autoGainControl"] = autoGainControl
    o["noiseSuppression"] = noiseSuppression
    o["latency"] = latency
    o["channelCount"] = channelCount
    o["deviceId"] = deviceId
    o["groupId"] = groupId
    return o
}

public external interface MediaTrackConstraintSet {
    var width: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var height: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var aspectRatio: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var frameRate: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var facingMode: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var resizeMode: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var volume: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var sampleRate: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var sampleSize: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var echoCancellation: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var autoGainControl: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var noiseSuppression: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var latency: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var channelCount: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var deviceId: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var groupId: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun MediaTrackConstraintSet(width: dynamic = undefined, height: dynamic = undefined, aspectRatio: dynamic = undefined, frameRate: dynamic = undefined, facingMode: dynamic = undefined, resizeMode: dynamic = undefined, volume: dynamic = undefined, sampleRate: dynamic = undefined, sampleSize: dynamic = undefined, echoCancellation: dynamic = undefined, autoGainControl: dynamic = undefined, noiseSuppression: dynamic = undefined, latency: dynamic = undefined, channelCount: dynamic = undefined, deviceId: dynamic = undefined, groupId: dynamic = undefined): MediaTrackConstraintSet {
    konst o = js("({})")
    o["width"] = width
    o["height"] = height
    o["aspectRatio"] = aspectRatio
    o["frameRate"] = frameRate
    o["facingMode"] = facingMode
    o["resizeMode"] = resizeMode
    o["volume"] = volume
    o["sampleRate"] = sampleRate
    o["sampleSize"] = sampleSize
    o["echoCancellation"] = echoCancellation
    o["autoGainControl"] = autoGainControl
    o["noiseSuppression"] = noiseSuppression
    o["latency"] = latency
    o["channelCount"] = channelCount
    o["deviceId"] = deviceId
    o["groupId"] = groupId
    return o
}

/**
 * Exposes the JavaScript [MediaTrackSettings](https://developer.mozilla.org/en/docs/Web/API/MediaTrackSettings) to Kotlin
 */
public external interface MediaTrackSettings {
    var width: Int?
        get() = definedExternally
        set(konstue) = definedExternally
    var height: Int?
        get() = definedExternally
        set(konstue) = definedExternally
    var aspectRatio: Double?
        get() = definedExternally
        set(konstue) = definedExternally
    var frameRate: Double?
        get() = definedExternally
        set(konstue) = definedExternally
    var facingMode: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var resizeMode: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var volume: Double?
        get() = definedExternally
        set(konstue) = definedExternally
    var sampleRate: Int?
        get() = definedExternally
        set(konstue) = definedExternally
    var sampleSize: Int?
        get() = definedExternally
        set(konstue) = definedExternally
    var echoCancellation: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
    var autoGainControl: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
    var noiseSuppression: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
    var latency: Double?
        get() = definedExternally
        set(konstue) = definedExternally
    var channelCount: Int?
        get() = definedExternally
        set(konstue) = definedExternally
    var deviceId: String?
        get() = definedExternally
        set(konstue) = definedExternally
    var groupId: String?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun MediaTrackSettings(width: Int? = undefined, height: Int? = undefined, aspectRatio: Double? = undefined, frameRate: Double? = undefined, facingMode: String? = undefined, resizeMode: String? = undefined, volume: Double? = undefined, sampleRate: Int? = undefined, sampleSize: Int? = undefined, echoCancellation: Boolean? = undefined, autoGainControl: Boolean? = undefined, noiseSuppression: Boolean? = undefined, latency: Double? = undefined, channelCount: Int? = undefined, deviceId: String? = undefined, groupId: String? = undefined): MediaTrackSettings {
    konst o = js("({})")
    o["width"] = width
    o["height"] = height
    o["aspectRatio"] = aspectRatio
    o["frameRate"] = frameRate
    o["facingMode"] = facingMode
    o["resizeMode"] = resizeMode
    o["volume"] = volume
    o["sampleRate"] = sampleRate
    o["sampleSize"] = sampleSize
    o["echoCancellation"] = echoCancellation
    o["autoGainControl"] = autoGainControl
    o["noiseSuppression"] = noiseSuppression
    o["latency"] = latency
    o["channelCount"] = channelCount
    o["deviceId"] = deviceId
    o["groupId"] = groupId
    return o
}

/**
 * Exposes the JavaScript [MediaStreamTrackEvent](https://developer.mozilla.org/en/docs/Web/API/MediaStreamTrackEvent) to Kotlin
 */
public external open class MediaStreamTrackEvent(type: String, eventInitDict: MediaStreamTrackEventInit) : Event {
    open konst track: MediaStreamTrack

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface MediaStreamTrackEventInit : EventInit {
    var track: MediaStreamTrack?
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun MediaStreamTrackEventInit(track: MediaStreamTrack?, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): MediaStreamTrackEventInit {
    konst o = js("({})")
    o["track"] = track
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

public external open class OverconstrainedErrorEvent(type: String, eventInitDict: OverconstrainedErrorEventInit) : Event {
    open konst error: dynamic

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface OverconstrainedErrorEventInit : EventInit {
    var error: dynamic /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun OverconstrainedErrorEventInit(error: dynamic = null, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): OverconstrainedErrorEventInit {
    konst o = js("({})")
    o["error"] = error
    o["bubbles"] = bubbles
    o["cancelable"] = cancelable
    o["composed"] = composed
    return o
}

/**
 * Exposes the JavaScript [MediaDevices](https://developer.mozilla.org/en/docs/Web/API/MediaDevices) to Kotlin
 */
public external abstract class MediaDevices : EventTarget {
    open var ondevicechange: ((Event) -> dynamic)?
    fun enumerateDevices(): Promise<Array<MediaDeviceInfo>>
    fun getSupportedConstraints(): MediaTrackSupportedConstraints
    fun getUserMedia(constraints: MediaStreamConstraints = definedExternally): Promise<MediaStream>
}

/**
 * Exposes the JavaScript [MediaDeviceInfo](https://developer.mozilla.org/en/docs/Web/API/MediaDeviceInfo) to Kotlin
 */
public external abstract class MediaDeviceInfo {
    open konst deviceId: String
    open konst kind: MediaDeviceKind
    open konst label: String
    open konst groupId: String
    fun toJSON(): dynamic
}

public external abstract class InputDeviceInfo : MediaDeviceInfo {
    fun getCapabilities(): MediaTrackCapabilities
}

/**
 * Exposes the JavaScript [MediaStreamConstraints](https://developer.mozilla.org/en/docs/Web/API/MediaStreamConstraints) to Kotlin
 */
public external interface MediaStreamConstraints {
    var video: dynamic /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var audio: dynamic /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun MediaStreamConstraints(video: dynamic = false, audio: dynamic = false): MediaStreamConstraints {
    konst o = js("({})")
    o["video"] = video
    o["audio"] = audio
    return o
}

public external interface ConstrainablePattern {
    var onoverconstrained: ((Event) -> dynamic)?
        get() = definedExternally
        set(konstue) = definedExternally
    fun getCapabilities(): Capabilities
    fun getConstraints(): Constraints
    fun getSettings(): Settings
    fun applyConstraints(constraints: Constraints = definedExternally): Promise<Unit>
}

/**
 * Exposes the JavaScript [DoubleRange](https://developer.mozilla.org/en/docs/Web/API/DoubleRange) to Kotlin
 */
public external interface DoubleRange {
    var max: Double?
        get() = definedExternally
        set(konstue) = definedExternally
    var min: Double?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun DoubleRange(max: Double? = undefined, min: Double? = undefined): DoubleRange {
    konst o = js("({})")
    o["max"] = max
    o["min"] = min
    return o
}

public external interface ConstrainDoubleRange : DoubleRange {
    var exact: Double?
        get() = definedExternally
        set(konstue) = definedExternally
    var ideal: Double?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun ConstrainDoubleRange(exact: Double? = undefined, ideal: Double? = undefined, max: Double? = undefined, min: Double? = undefined): ConstrainDoubleRange {
    konst o = js("({})")
    o["exact"] = exact
    o["ideal"] = ideal
    o["max"] = max
    o["min"] = min
    return o
}

public external interface ULongRange {
    var max: Int?
        get() = definedExternally
        set(konstue) = definedExternally
    var min: Int?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun ULongRange(max: Int? = undefined, min: Int? = undefined): ULongRange {
    konst o = js("({})")
    o["max"] = max
    o["min"] = min
    return o
}

public external interface ConstrainULongRange : ULongRange {
    var exact: Int?
        get() = definedExternally
        set(konstue) = definedExternally
    var ideal: Int?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun ConstrainULongRange(exact: Int? = undefined, ideal: Int? = undefined, max: Int? = undefined, min: Int? = undefined): ConstrainULongRange {
    konst o = js("({})")
    o["exact"] = exact
    o["ideal"] = ideal
    o["max"] = max
    o["min"] = min
    return o
}

/**
 * Exposes the JavaScript [ConstrainBooleanParameters](https://developer.mozilla.org/en/docs/Web/API/ConstrainBooleanParameters) to Kotlin
 */
public external interface ConstrainBooleanParameters {
    var exact: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
    var ideal: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun ConstrainBooleanParameters(exact: Boolean? = undefined, ideal: Boolean? = undefined): ConstrainBooleanParameters {
    konst o = js("({})")
    o["exact"] = exact
    o["ideal"] = ideal
    return o
}

/**
 * Exposes the JavaScript [ConstrainDOMStringParameters](https://developer.mozilla.org/en/docs/Web/API/ConstrainDOMStringParameters) to Kotlin
 */
public external interface ConstrainDOMStringParameters {
    var exact: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
    var ideal: dynamic
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun ConstrainDOMStringParameters(exact: dynamic = undefined, ideal: dynamic = undefined): ConstrainDOMStringParameters {
    konst o = js("({})")
    o["exact"] = exact
    o["ideal"] = ideal
    return o
}

public external interface Capabilities

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun Capabilities(): Capabilities {
    konst o = js("({})")
    return o
}

public external interface Settings

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun Settings(): Settings {
    konst o = js("({})")
    return o
}

public external interface ConstraintSet

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun ConstraintSet(): ConstraintSet {
    konst o = js("({})")
    return o
}

public external interface Constraints : ConstraintSet {
    var advanced: Array<ConstraintSet>?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun Constraints(advanced: Array<ConstraintSet>? = undefined): Constraints {
    konst o = js("({})")
    o["advanced"] = advanced
    return o
}

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface MediaStreamTrackState {
    companion object
}

public inline konst MediaStreamTrackState.Companion.LIVE: MediaStreamTrackState get() = "live".asDynamic().unsafeCast<MediaStreamTrackState>()

public inline konst MediaStreamTrackState.Companion.ENDED: MediaStreamTrackState get() = "ended".asDynamic().unsafeCast<MediaStreamTrackState>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface VideoFacingModeEnum {
    companion object
}

public inline konst VideoFacingModeEnum.Companion.USER: VideoFacingModeEnum get() = "user".asDynamic().unsafeCast<VideoFacingModeEnum>()

public inline konst VideoFacingModeEnum.Companion.ENVIRONMENT: VideoFacingModeEnum get() = "environment".asDynamic().unsafeCast<VideoFacingModeEnum>()

public inline konst VideoFacingModeEnum.Companion.LEFT: VideoFacingModeEnum get() = "left".asDynamic().unsafeCast<VideoFacingModeEnum>()

public inline konst VideoFacingModeEnum.Companion.RIGHT: VideoFacingModeEnum get() = "right".asDynamic().unsafeCast<VideoFacingModeEnum>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface VideoResizeModeEnum {
    companion object
}

public inline konst VideoResizeModeEnum.Companion.NONE: VideoResizeModeEnum get() = "none".asDynamic().unsafeCast<VideoResizeModeEnum>()

public inline konst VideoResizeModeEnum.Companion.CROP_AND_SCALE: VideoResizeModeEnum get() = "crop-and-scale".asDynamic().unsafeCast<VideoResizeModeEnum>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface MediaDeviceKind {
    companion object
}

public inline konst MediaDeviceKind.Companion.AUDIOINPUT: MediaDeviceKind get() = "audioinput".asDynamic().unsafeCast<MediaDeviceKind>()

public inline konst MediaDeviceKind.Companion.AUDIOOUTPUT: MediaDeviceKind get() = "audiooutput".asDynamic().unsafeCast<MediaDeviceKind>()

public inline konst MediaDeviceKind.Companion.VIDEOINPUT: MediaDeviceKind get() = "videoinput".asDynamic().unsafeCast<MediaDeviceKind>()