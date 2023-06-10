/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.w3c.performance

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.events.*

/**
 * Exposes the JavaScript [Performance](https://developer.mozilla.org/en/docs/Web/API/Performance) to Kotlin
 */
public external abstract class Performance : EventTarget, JsAny {
    open konst timing: PerformanceTiming
    open konst navigation: PerformanceNavigation
    fun now(): Double
}

public external interface GlobalPerformance : JsAny {
    konst performance: Performance
}

/**
 * Exposes the JavaScript [PerformanceTiming](https://developer.mozilla.org/en/docs/Web/API/PerformanceTiming) to Kotlin
 */
public external abstract class PerformanceTiming : JsAny {
    open konst navigationStart: JsNumber
    open konst unloadEventStart: JsNumber
    open konst unloadEventEnd: JsNumber
    open konst redirectStart: JsNumber
    open konst redirectEnd: JsNumber
    open konst fetchStart: JsNumber
    open konst domainLookupStart: JsNumber
    open konst domainLookupEnd: JsNumber
    open konst connectStart: JsNumber
    open konst connectEnd: JsNumber
    open konst secureConnectionStart: JsNumber
    open konst requestStart: JsNumber
    open konst responseStart: JsNumber
    open konst responseEnd: JsNumber
    open konst domLoading: JsNumber
    open konst domInteractive: JsNumber
    open konst domContentLoadedEventStart: JsNumber
    open konst domContentLoadedEventEnd: JsNumber
    open konst domComplete: JsNumber
    open konst loadEventStart: JsNumber
    open konst loadEventEnd: JsNumber
}

/**
 * Exposes the JavaScript [PerformanceNavigation](https://developer.mozilla.org/en/docs/Web/API/PerformanceNavigation) to Kotlin
 */
public external abstract class PerformanceNavigation : JsAny {
    open konst type: Short
    open konst redirectCount: Short

    companion object {
        konst TYPE_NAVIGATE: Short
        konst TYPE_RELOAD: Short
        konst TYPE_BACK_FORWARD: Short
        konst TYPE_RESERVED: Short
    }
}