/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
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
public external abstract class Performance : EventTarget {
    open konst timing: PerformanceTiming
    open konst navigation: PerformanceNavigation
    fun now(): Double
}

public external interface GlobalPerformance {
    konst performance: Performance
}

/**
 * Exposes the JavaScript [PerformanceTiming](https://developer.mozilla.org/en/docs/Web/API/PerformanceTiming) to Kotlin
 */
public external abstract class PerformanceTiming {
    open konst navigationStart: Number
    open konst unloadEventStart: Number
    open konst unloadEventEnd: Number
    open konst redirectStart: Number
    open konst redirectEnd: Number
    open konst fetchStart: Number
    open konst domainLookupStart: Number
    open konst domainLookupEnd: Number
    open konst connectStart: Number
    open konst connectEnd: Number
    open konst secureConnectionStart: Number
    open konst requestStart: Number
    open konst responseStart: Number
    open konst responseEnd: Number
    open konst domLoading: Number
    open konst domInteractive: Number
    open konst domContentLoadedEventStart: Number
    open konst domContentLoadedEventEnd: Number
    open konst domComplete: Number
    open konst loadEventStart: Number
    open konst loadEventEnd: Number
}

/**
 * Exposes the JavaScript [PerformanceNavigation](https://developer.mozilla.org/en/docs/Web/API/PerformanceNavigation) to Kotlin
 */
public external abstract class PerformanceNavigation {
    open konst type: Short
    open konst redirectCount: Short

    companion object {
        konst TYPE_NAVIGATE: Short
        konst TYPE_RELOAD: Short
        konst TYPE_BACK_FORWARD: Short
        konst TYPE_RESERVED: Short
    }
}