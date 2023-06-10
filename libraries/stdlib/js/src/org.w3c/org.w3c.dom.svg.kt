/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.w3c.dom.svg

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.css.*

/**
 * Exposes the JavaScript [SVGElement](https://developer.mozilla.org/en/docs/Web/API/SVGElement) to Kotlin
 */
public external abstract class SVGElement : Element, ElementCSSInlineStyle, GlobalEventHandlers, SVGElementInstance {
    open konst dataset: DOMStringMap
    open konst ownerSVGElement: SVGSVGElement?
    open konst viewportElement: SVGElement?
    open var tabIndex: Int
    fun focus()
    fun blur()

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external interface SVGBoundingBoxOptions {
    var fill: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var stroke: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var markers: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var clipped: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline fun SVGBoundingBoxOptions(fill: Boolean? = true, stroke: Boolean? = false, markers: Boolean? = false, clipped: Boolean? = false): SVGBoundingBoxOptions {
    konst o = js("({})")
    o["fill"] = fill
    o["stroke"] = stroke
    o["markers"] = markers
    o["clipped"] = clipped
    return o
}

/**
 * Exposes the JavaScript [SVGGraphicsElement](https://developer.mozilla.org/en/docs/Web/API/SVGGraphicsElement) to Kotlin
 */
public external abstract class SVGGraphicsElement : SVGElement, SVGTests {
    open konst transform: SVGAnimatedTransformList
    fun getBBox(options: SVGBoundingBoxOptions = definedExternally): DOMRect
    fun getCTM(): DOMMatrix?
    fun getScreenCTM(): DOMMatrix?

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGGeometryElement](https://developer.mozilla.org/en/docs/Web/API/SVGGeometryElement) to Kotlin
 */
public external abstract class SVGGeometryElement : SVGGraphicsElement {
    open konst pathLength: SVGAnimatedNumber
    fun isPointInFill(point: DOMPoint): Boolean
    fun isPointInStroke(point: DOMPoint): Boolean
    fun getTotalLength(): Float
    fun getPointAtLength(distance: Float): DOMPoint

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGNumber](https://developer.mozilla.org/en/docs/Web/API/SVGNumber) to Kotlin
 */
public external abstract class SVGNumber {
    open var konstue: Float
}

/**
 * Exposes the JavaScript [SVGLength](https://developer.mozilla.org/en/docs/Web/API/SVGLength) to Kotlin
 */
public external abstract class SVGLength {
    open konst unitType: Short
    open var konstue: Float
    open var konstueInSpecifiedUnits: Float
    open var konstueAsString: String
    fun newValueSpecifiedUnits(unitType: Short, konstueInSpecifiedUnits: Float)
    fun convertToSpecifiedUnits(unitType: Short)

    companion object {
        konst SVG_LENGTHTYPE_UNKNOWN: Short
        konst SVG_LENGTHTYPE_NUMBER: Short
        konst SVG_LENGTHTYPE_PERCENTAGE: Short
        konst SVG_LENGTHTYPE_EMS: Short
        konst SVG_LENGTHTYPE_EXS: Short
        konst SVG_LENGTHTYPE_PX: Short
        konst SVG_LENGTHTYPE_CM: Short
        konst SVG_LENGTHTYPE_MM: Short
        konst SVG_LENGTHTYPE_IN: Short
        konst SVG_LENGTHTYPE_PT: Short
        konst SVG_LENGTHTYPE_PC: Short
    }
}

/**
 * Exposes the JavaScript [SVGAngle](https://developer.mozilla.org/en/docs/Web/API/SVGAngle) to Kotlin
 */
public external abstract class SVGAngle {
    open konst unitType: Short
    open var konstue: Float
    open var konstueInSpecifiedUnits: Float
    open var konstueAsString: String
    fun newValueSpecifiedUnits(unitType: Short, konstueInSpecifiedUnits: Float)
    fun convertToSpecifiedUnits(unitType: Short)

    companion object {
        konst SVG_ANGLETYPE_UNKNOWN: Short
        konst SVG_ANGLETYPE_UNSPECIFIED: Short
        konst SVG_ANGLETYPE_DEG: Short
        konst SVG_ANGLETYPE_RAD: Short
        konst SVG_ANGLETYPE_GRAD: Short
    }
}

public external abstract class SVGNameList {
    open konst length: Int
    open konst numberOfItems: Int
    fun clear()
    fun initialize(newItem: dynamic): dynamic
    fun insertItemBefore(newItem: dynamic, index: Int): dynamic
    fun replaceItem(newItem: dynamic, index: Int): dynamic
    fun removeItem(index: Int): dynamic
    fun appendItem(newItem: dynamic): dynamic
    fun getItem(index: Int): dynamic
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGNameList.get(index: Int): dynamic = asDynamic()[index]

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGNameList.set(index: Int, newItem: dynamic) { asDynamic()[index] = newItem }

/**
 * Exposes the JavaScript [SVGNumberList](https://developer.mozilla.org/en/docs/Web/API/SVGNumberList) to Kotlin
 */
public external abstract class SVGNumberList {
    open konst length: Int
    open konst numberOfItems: Int
    fun clear()
    fun initialize(newItem: SVGNumber): SVGNumber
    fun insertItemBefore(newItem: SVGNumber, index: Int): SVGNumber
    fun replaceItem(newItem: SVGNumber, index: Int): SVGNumber
    fun removeItem(index: Int): SVGNumber
    fun appendItem(newItem: SVGNumber): SVGNumber
    fun getItem(index: Int): SVGNumber
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGNumberList.get(index: Int): SVGNumber? = asDynamic()[index]

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGNumberList.set(index: Int, newItem: SVGNumber) { asDynamic()[index] = newItem }

/**
 * Exposes the JavaScript [SVGLengthList](https://developer.mozilla.org/en/docs/Web/API/SVGLengthList) to Kotlin
 */
public external abstract class SVGLengthList {
    open konst length: Int
    open konst numberOfItems: Int
    fun clear()
    fun initialize(newItem: SVGLength): SVGLength
    fun insertItemBefore(newItem: SVGLength, index: Int): SVGLength
    fun replaceItem(newItem: SVGLength, index: Int): SVGLength
    fun removeItem(index: Int): SVGLength
    fun appendItem(newItem: SVGLength): SVGLength
    fun getItem(index: Int): SVGLength
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGLengthList.get(index: Int): SVGLength? = asDynamic()[index]

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGLengthList.set(index: Int, newItem: SVGLength) { asDynamic()[index] = newItem }

/**
 * Exposes the JavaScript [SVGAnimatedBoolean](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedBoolean) to Kotlin
 */
public external abstract class SVGAnimatedBoolean {
    open var baseVal: Boolean
    open konst animVal: Boolean
}

/**
 * Exposes the JavaScript [SVGAnimatedEnumeration](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedEnumeration) to Kotlin
 */
public external abstract class SVGAnimatedEnumeration {
    open var baseVal: Short
    open konst animVal: Short
}

/**
 * Exposes the JavaScript [SVGAnimatedInteger](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedInteger) to Kotlin
 */
public external abstract class SVGAnimatedInteger {
    open var baseVal: Int
    open konst animVal: Int
}

/**
 * Exposes the JavaScript [SVGAnimatedNumber](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedNumber) to Kotlin
 */
public external abstract class SVGAnimatedNumber {
    open var baseVal: Float
    open konst animVal: Float
}

/**
 * Exposes the JavaScript [SVGAnimatedLength](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedLength) to Kotlin
 */
public external abstract class SVGAnimatedLength {
    open konst baseVal: SVGLength
    open konst animVal: SVGLength
}

/**
 * Exposes the JavaScript [SVGAnimatedAngle](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedAngle) to Kotlin
 */
public external abstract class SVGAnimatedAngle {
    open konst baseVal: SVGAngle
    open konst animVal: SVGAngle
}

/**
 * Exposes the JavaScript [SVGAnimatedString](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedString) to Kotlin
 */
public external abstract class SVGAnimatedString {
    open var baseVal: String
    open konst animVal: String
}

/**
 * Exposes the JavaScript [SVGAnimatedRect](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedRect) to Kotlin
 */
public external abstract class SVGAnimatedRect {
    open konst baseVal: DOMRect
    open konst animVal: DOMRectReadOnly
}

/**
 * Exposes the JavaScript [SVGAnimatedNumberList](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedNumberList) to Kotlin
 */
public external abstract class SVGAnimatedNumberList {
    open konst baseVal: SVGNumberList
    open konst animVal: SVGNumberList
}

/**
 * Exposes the JavaScript [SVGAnimatedLengthList](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedLengthList) to Kotlin
 */
public external abstract class SVGAnimatedLengthList {
    open konst baseVal: SVGLengthList
    open konst animVal: SVGLengthList
}

/**
 * Exposes the JavaScript [SVGStringList](https://developer.mozilla.org/en/docs/Web/API/SVGStringList) to Kotlin
 */
public external abstract class SVGStringList {
    open konst length: Int
    open konst numberOfItems: Int
    fun clear()
    fun initialize(newItem: String): String
    fun insertItemBefore(newItem: String, index: Int): String
    fun replaceItem(newItem: String, index: Int): String
    fun removeItem(index: Int): String
    fun appendItem(newItem: String): String
    fun getItem(index: Int): String
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGStringList.get(index: Int): String? = asDynamic()[index]

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGStringList.set(index: Int, newItem: String) { asDynamic()[index] = newItem }

/**
 * Exposes the JavaScript [SVGUnitTypes](https://developer.mozilla.org/en/docs/Web/API/SVGUnitTypes) to Kotlin
 */
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface SVGUnitTypes {
    companion object {
        konst SVG_UNIT_TYPE_UNKNOWN: Short
        konst SVG_UNIT_TYPE_USERSPACEONUSE: Short
        konst SVG_UNIT_TYPE_OBJECTBOUNDINGBOX: Short
    }
}

/**
 * Exposes the JavaScript [SVGTests](https://developer.mozilla.org/en/docs/Web/API/SVGTests) to Kotlin
 */
public external interface SVGTests {
    konst requiredExtensions: SVGStringList
    konst systemLanguage: SVGStringList
}

public external interface SVGFitToViewBox {
    konst viewBox: SVGAnimatedRect
    konst preserveAspectRatio: SVGAnimatedPreserveAspectRatio
}

/**
 * Exposes the JavaScript [SVGZoomAndPan](https://developer.mozilla.org/en/docs/Web/API/SVGZoomAndPan) to Kotlin
 */
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface SVGZoomAndPan {
    var zoomAndPan: Short

    companion object {
        konst SVG_ZOOMANDPAN_UNKNOWN: Short
        konst SVG_ZOOMANDPAN_DISABLE: Short
        konst SVG_ZOOMANDPAN_MAGNIFY: Short
    }
}

/**
 * Exposes the JavaScript [SVGURIReference](https://developer.mozilla.org/en/docs/Web/API/SVGURIReference) to Kotlin
 */
public external interface SVGURIReference {
    konst href: SVGAnimatedString
}

/**
 * Exposes the JavaScript [SVGSVGElement](https://developer.mozilla.org/en/docs/Web/API/SVGSVGElement) to Kotlin
 */
public external abstract class SVGSVGElement : SVGGraphicsElement, SVGFitToViewBox, SVGZoomAndPan, WindowEventHandlers {
    open konst x: SVGAnimatedLength
    open konst y: SVGAnimatedLength
    open konst width: SVGAnimatedLength
    open konst height: SVGAnimatedLength
    open var currentScale: Float
    open konst currentTranslate: DOMPointReadOnly
    fun getIntersectionList(rect: DOMRectReadOnly, referenceElement: SVGElement?): NodeList
    fun getEnclosureList(rect: DOMRectReadOnly, referenceElement: SVGElement?): NodeList
    fun checkIntersection(element: SVGElement, rect: DOMRectReadOnly): Boolean
    fun checkEnclosure(element: SVGElement, rect: DOMRectReadOnly): Boolean
    fun deselectAll()
    fun createSVGNumber(): SVGNumber
    fun createSVGLength(): SVGLength
    fun createSVGAngle(): SVGAngle
    fun createSVGPoint(): DOMPoint
    fun createSVGMatrix(): DOMMatrix
    fun createSVGRect(): DOMRect
    fun createSVGTransform(): SVGTransform
    fun createSVGTransformFromMatrix(matrix: DOMMatrixReadOnly): SVGTransform
    fun getElementById(elementId: String): Element
    fun suspendRedraw(maxWaitMilliseconds: Int): Int
    fun unsuspendRedraw(suspendHandleID: Int)
    fun unsuspendRedrawAll()
    fun forceRedraw()

    companion object {
        konst SVG_ZOOMANDPAN_UNKNOWN: Short
        konst SVG_ZOOMANDPAN_DISABLE: Short
        konst SVG_ZOOMANDPAN_MAGNIFY: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGGElement](https://developer.mozilla.org/en/docs/Web/API/SVGGElement) to Kotlin
 */
public external abstract class SVGGElement : SVGGraphicsElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external abstract class SVGUnknownElement : SVGGraphicsElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGDefsElement](https://developer.mozilla.org/en/docs/Web/API/SVGDefsElement) to Kotlin
 */
public external abstract class SVGDefsElement : SVGGraphicsElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGDescElement](https://developer.mozilla.org/en/docs/Web/API/SVGDescElement) to Kotlin
 */
public external abstract class SVGDescElement : SVGElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGMetadataElement](https://developer.mozilla.org/en/docs/Web/API/SVGMetadataElement) to Kotlin
 */
public external abstract class SVGMetadataElement : SVGElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGTitleElement](https://developer.mozilla.org/en/docs/Web/API/SVGTitleElement) to Kotlin
 */
public external abstract class SVGTitleElement : SVGElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGSymbolElement](https://developer.mozilla.org/en/docs/Web/API/SVGSymbolElement) to Kotlin
 */
public external abstract class SVGSymbolElement : SVGGraphicsElement, SVGFitToViewBox {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGUseElement](https://developer.mozilla.org/en/docs/Web/API/SVGUseElement) to Kotlin
 */
public external abstract class SVGUseElement : SVGGraphicsElement, SVGURIReference {
    open konst x: SVGAnimatedLength
    open konst y: SVGAnimatedLength
    open konst width: SVGAnimatedLength
    open konst height: SVGAnimatedLength
    open konst instanceRoot: SVGElement?
    open konst animatedInstanceRoot: SVGElement?

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external open class SVGUseElementShadowRoot : ShadowRoot {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external interface SVGElementInstance {
    konst correspondingElement: SVGElement?
        get() = definedExternally
    konst correspondingUseElement: SVGUseElement?
        get() = definedExternally
}

public external open class ShadowAnimation(source: dynamic, newTarget: dynamic) {
    open konst sourceAnimation: dynamic
}

/**
 * Exposes the JavaScript [SVGSwitchElement](https://developer.mozilla.org/en/docs/Web/API/SVGSwitchElement) to Kotlin
 */
public external abstract class SVGSwitchElement : SVGGraphicsElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external interface GetSVGDocument {
    fun getSVGDocument(): Document
}

/**
 * Exposes the JavaScript [SVGStyleElement](https://developer.mozilla.org/en/docs/Web/API/SVGStyleElement) to Kotlin
 */
public external abstract class SVGStyleElement : SVGElement, LinkStyle {
    open var type: String
    open var media: String
    open var title: String

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGTransform](https://developer.mozilla.org/en/docs/Web/API/SVGTransform) to Kotlin
 */
public external abstract class SVGTransform {
    open konst type: Short
    open konst matrix: DOMMatrix
    open konst angle: Float
    fun setMatrix(matrix: DOMMatrixReadOnly)
    fun setTranslate(tx: Float, ty: Float)
    fun setScale(sx: Float, sy: Float)
    fun setRotate(angle: Float, cx: Float, cy: Float)
    fun setSkewX(angle: Float)
    fun setSkewY(angle: Float)

    companion object {
        konst SVG_TRANSFORM_UNKNOWN: Short
        konst SVG_TRANSFORM_MATRIX: Short
        konst SVG_TRANSFORM_TRANSLATE: Short
        konst SVG_TRANSFORM_SCALE: Short
        konst SVG_TRANSFORM_ROTATE: Short
        konst SVG_TRANSFORM_SKEWX: Short
        konst SVG_TRANSFORM_SKEWY: Short
    }
}

/**
 * Exposes the JavaScript [SVGTransformList](https://developer.mozilla.org/en/docs/Web/API/SVGTransformList) to Kotlin
 */
public external abstract class SVGTransformList {
    open konst length: Int
    open konst numberOfItems: Int
    fun clear()
    fun initialize(newItem: SVGTransform): SVGTransform
    fun insertItemBefore(newItem: SVGTransform, index: Int): SVGTransform
    fun replaceItem(newItem: SVGTransform, index: Int): SVGTransform
    fun removeItem(index: Int): SVGTransform
    fun appendItem(newItem: SVGTransform): SVGTransform
    fun createSVGTransformFromMatrix(matrix: DOMMatrixReadOnly): SVGTransform
    fun consolidate(): SVGTransform?
    fun getItem(index: Int): SVGTransform
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGTransformList.get(index: Int): SVGTransform? = asDynamic()[index]

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGTransformList.set(index: Int, newItem: SVGTransform) { asDynamic()[index] = newItem }

/**
 * Exposes the JavaScript [SVGAnimatedTransformList](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedTransformList) to Kotlin
 */
public external abstract class SVGAnimatedTransformList {
    open konst baseVal: SVGTransformList
    open konst animVal: SVGTransformList
}

/**
 * Exposes the JavaScript [SVGPreserveAspectRatio](https://developer.mozilla.org/en/docs/Web/API/SVGPreserveAspectRatio) to Kotlin
 */
public external abstract class SVGPreserveAspectRatio {
    open var align: Short
    open var meetOrSlice: Short

    companion object {
        konst SVG_PRESERVEASPECTRATIO_UNKNOWN: Short
        konst SVG_PRESERVEASPECTRATIO_NONE: Short
        konst SVG_PRESERVEASPECTRATIO_XMINYMIN: Short
        konst SVG_PRESERVEASPECTRATIO_XMIDYMIN: Short
        konst SVG_PRESERVEASPECTRATIO_XMAXYMIN: Short
        konst SVG_PRESERVEASPECTRATIO_XMINYMID: Short
        konst SVG_PRESERVEASPECTRATIO_XMIDYMID: Short
        konst SVG_PRESERVEASPECTRATIO_XMAXYMID: Short
        konst SVG_PRESERVEASPECTRATIO_XMINYMAX: Short
        konst SVG_PRESERVEASPECTRATIO_XMIDYMAX: Short
        konst SVG_PRESERVEASPECTRATIO_XMAXYMAX: Short
        konst SVG_MEETORSLICE_UNKNOWN: Short
        konst SVG_MEETORSLICE_MEET: Short
        konst SVG_MEETORSLICE_SLICE: Short
    }
}

/**
 * Exposes the JavaScript [SVGAnimatedPreserveAspectRatio](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedPreserveAspectRatio) to Kotlin
 */
public external abstract class SVGAnimatedPreserveAspectRatio {
    open konst baseVal: SVGPreserveAspectRatio
    open konst animVal: SVGPreserveAspectRatio
}

/**
 * Exposes the JavaScript [SVGPathElement](https://developer.mozilla.org/en/docs/Web/API/SVGPathElement) to Kotlin
 */
public external abstract class SVGPathElement : SVGGeometryElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGRectElement](https://developer.mozilla.org/en/docs/Web/API/SVGRectElement) to Kotlin
 */
public external abstract class SVGRectElement : SVGGeometryElement {
    open konst x: SVGAnimatedLength
    open konst y: SVGAnimatedLength
    open konst width: SVGAnimatedLength
    open konst height: SVGAnimatedLength
    open konst rx: SVGAnimatedLength
    open konst ry: SVGAnimatedLength

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGCircleElement](https://developer.mozilla.org/en/docs/Web/API/SVGCircleElement) to Kotlin
 */
public external abstract class SVGCircleElement : SVGGeometryElement {
    open konst cx: SVGAnimatedLength
    open konst cy: SVGAnimatedLength
    open konst r: SVGAnimatedLength

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGEllipseElement](https://developer.mozilla.org/en/docs/Web/API/SVGEllipseElement) to Kotlin
 */
public external abstract class SVGEllipseElement : SVGGeometryElement {
    open konst cx: SVGAnimatedLength
    open konst cy: SVGAnimatedLength
    open konst rx: SVGAnimatedLength
    open konst ry: SVGAnimatedLength

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGLineElement](https://developer.mozilla.org/en/docs/Web/API/SVGLineElement) to Kotlin
 */
public external abstract class SVGLineElement : SVGGeometryElement {
    open konst x1: SVGAnimatedLength
    open konst y1: SVGAnimatedLength
    open konst x2: SVGAnimatedLength
    open konst y2: SVGAnimatedLength

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGMeshElement](https://developer.mozilla.org/en/docs/Web/API/SVGMeshElement) to Kotlin
 */
public external abstract class SVGMeshElement : SVGGeometryElement, SVGURIReference {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGAnimatedPoints](https://developer.mozilla.org/en/docs/Web/API/SVGAnimatedPoints) to Kotlin
 */
public external interface SVGAnimatedPoints {
    konst points: SVGPointList
    konst animatedPoints: SVGPointList
}

public external abstract class SVGPointList {
    open konst length: Int
    open konst numberOfItems: Int
    fun clear()
    fun initialize(newItem: DOMPoint): DOMPoint
    fun insertItemBefore(newItem: DOMPoint, index: Int): DOMPoint
    fun replaceItem(newItem: DOMPoint, index: Int): DOMPoint
    fun removeItem(index: Int): DOMPoint
    fun appendItem(newItem: DOMPoint): DOMPoint
    fun getItem(index: Int): DOMPoint
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGPointList.get(index: Int): DOMPoint? = asDynamic()[index]

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.InlineOnly
public inline operator fun SVGPointList.set(index: Int, newItem: DOMPoint) { asDynamic()[index] = newItem }

/**
 * Exposes the JavaScript [SVGPolylineElement](https://developer.mozilla.org/en/docs/Web/API/SVGPolylineElement) to Kotlin
 */
public external abstract class SVGPolylineElement : SVGGeometryElement, SVGAnimatedPoints {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGPolygonElement](https://developer.mozilla.org/en/docs/Web/API/SVGPolygonElement) to Kotlin
 */
public external abstract class SVGPolygonElement : SVGGeometryElement, SVGAnimatedPoints {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGTextContentElement](https://developer.mozilla.org/en/docs/Web/API/SVGTextContentElement) to Kotlin
 */
public external abstract class SVGTextContentElement : SVGGraphicsElement {
    open konst textLength: SVGAnimatedLength
    open konst lengthAdjust: SVGAnimatedEnumeration
    fun getNumberOfChars(): Int
    fun getComputedTextLength(): Float
    fun getSubStringLength(charnum: Int, nchars: Int): Float
    fun getStartPositionOfChar(charnum: Int): DOMPoint
    fun getEndPositionOfChar(charnum: Int): DOMPoint
    fun getExtentOfChar(charnum: Int): DOMRect
    fun getRotationOfChar(charnum: Int): Float
    fun getCharNumAtPosition(point: DOMPoint): Int
    fun selectSubString(charnum: Int, nchars: Int)

    companion object {
        konst LENGTHADJUST_UNKNOWN: Short
        konst LENGTHADJUST_SPACING: Short
        konst LENGTHADJUST_SPACINGANDGLYPHS: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGTextPositioningElement](https://developer.mozilla.org/en/docs/Web/API/SVGTextPositioningElement) to Kotlin
 */
public external abstract class SVGTextPositioningElement : SVGTextContentElement {
    open konst x: SVGAnimatedLengthList
    open konst y: SVGAnimatedLengthList
    open konst dx: SVGAnimatedLengthList
    open konst dy: SVGAnimatedLengthList
    open konst rotate: SVGAnimatedNumberList

    companion object {
        konst LENGTHADJUST_UNKNOWN: Short
        konst LENGTHADJUST_SPACING: Short
        konst LENGTHADJUST_SPACINGANDGLYPHS: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGTextElement](https://developer.mozilla.org/en/docs/Web/API/SVGTextElement) to Kotlin
 */
public external abstract class SVGTextElement : SVGTextPositioningElement {
    companion object {
        konst LENGTHADJUST_UNKNOWN: Short
        konst LENGTHADJUST_SPACING: Short
        konst LENGTHADJUST_SPACINGANDGLYPHS: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGTSpanElement](https://developer.mozilla.org/en/docs/Web/API/SVGTSpanElement) to Kotlin
 */
public external abstract class SVGTSpanElement : SVGTextPositioningElement {
    companion object {
        konst LENGTHADJUST_UNKNOWN: Short
        konst LENGTHADJUST_SPACING: Short
        konst LENGTHADJUST_SPACINGANDGLYPHS: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGTextPathElement](https://developer.mozilla.org/en/docs/Web/API/SVGTextPathElement) to Kotlin
 */
public external abstract class SVGTextPathElement : SVGTextContentElement, SVGURIReference {
    open konst startOffset: SVGAnimatedLength
    open konst method: SVGAnimatedEnumeration
    open konst spacing: SVGAnimatedEnumeration

    companion object {
        konst TEXTPATH_METHODTYPE_UNKNOWN: Short
        konst TEXTPATH_METHODTYPE_ALIGN: Short
        konst TEXTPATH_METHODTYPE_STRETCH: Short
        konst TEXTPATH_SPACINGTYPE_UNKNOWN: Short
        konst TEXTPATH_SPACINGTYPE_AUTO: Short
        konst TEXTPATH_SPACINGTYPE_EXACT: Short
        konst LENGTHADJUST_UNKNOWN: Short
        konst LENGTHADJUST_SPACING: Short
        konst LENGTHADJUST_SPACINGANDGLYPHS: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGImageElement](https://developer.mozilla.org/en/docs/Web/API/SVGImageElement) to Kotlin
 */
public external abstract class SVGImageElement : SVGGraphicsElement, SVGURIReference, HTMLOrSVGImageElement {
    open konst x: SVGAnimatedLength
    open konst y: SVGAnimatedLength
    open konst width: SVGAnimatedLength
    open konst height: SVGAnimatedLength
    open konst preserveAspectRatio: SVGAnimatedPreserveAspectRatio
    open var crossOrigin: String?

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGForeignObjectElement](https://developer.mozilla.org/en/docs/Web/API/SVGForeignObjectElement) to Kotlin
 */
public external abstract class SVGForeignObjectElement : SVGGraphicsElement {
    open konst x: SVGAnimatedLength
    open konst y: SVGAnimatedLength
    open konst width: SVGAnimatedLength
    open konst height: SVGAnimatedLength

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external abstract class SVGMarkerElement : SVGElement, SVGFitToViewBox {
    open konst refX: SVGAnimatedLength
    open konst refY: SVGAnimatedLength
    open konst markerUnits: SVGAnimatedEnumeration
    open konst markerWidth: SVGAnimatedLength
    open konst markerHeight: SVGAnimatedLength
    open konst orientType: SVGAnimatedEnumeration
    open konst orientAngle: SVGAnimatedAngle
    open var orient: String
    fun setOrientToAuto()
    fun setOrientToAngle(angle: SVGAngle)

    companion object {
        konst SVG_MARKERUNITS_UNKNOWN: Short
        konst SVG_MARKERUNITS_USERSPACEONUSE: Short
        konst SVG_MARKERUNITS_STROKEWIDTH: Short
        konst SVG_MARKER_ORIENT_UNKNOWN: Short
        konst SVG_MARKER_ORIENT_AUTO: Short
        konst SVG_MARKER_ORIENT_ANGLE: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGSolidcolorElement](https://developer.mozilla.org/en/docs/Web/API/SVGSolidcolorElement) to Kotlin
 */
public external abstract class SVGSolidcolorElement : SVGElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGGradientElement](https://developer.mozilla.org/en/docs/Web/API/SVGGradientElement) to Kotlin
 */
public external abstract class SVGGradientElement : SVGElement, SVGURIReference, SVGUnitTypes {
    open konst gradientUnits: SVGAnimatedEnumeration
    open konst gradientTransform: SVGAnimatedTransformList
    open konst spreadMethod: SVGAnimatedEnumeration

    companion object {
        konst SVG_SPREADMETHOD_UNKNOWN: Short
        konst SVG_SPREADMETHOD_PAD: Short
        konst SVG_SPREADMETHOD_REFLECT: Short
        konst SVG_SPREADMETHOD_REPEAT: Short
        konst SVG_UNIT_TYPE_UNKNOWN: Short
        konst SVG_UNIT_TYPE_USERSPACEONUSE: Short
        konst SVG_UNIT_TYPE_OBJECTBOUNDINGBOX: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGLinearGradientElement](https://developer.mozilla.org/en/docs/Web/API/SVGLinearGradientElement) to Kotlin
 */
public external abstract class SVGLinearGradientElement : SVGGradientElement {
    open konst x1: SVGAnimatedLength
    open konst y1: SVGAnimatedLength
    open konst x2: SVGAnimatedLength
    open konst y2: SVGAnimatedLength

    companion object {
        konst SVG_SPREADMETHOD_UNKNOWN: Short
        konst SVG_SPREADMETHOD_PAD: Short
        konst SVG_SPREADMETHOD_REFLECT: Short
        konst SVG_SPREADMETHOD_REPEAT: Short
        konst SVG_UNIT_TYPE_UNKNOWN: Short
        konst SVG_UNIT_TYPE_USERSPACEONUSE: Short
        konst SVG_UNIT_TYPE_OBJECTBOUNDINGBOX: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGRadialGradientElement](https://developer.mozilla.org/en/docs/Web/API/SVGRadialGradientElement) to Kotlin
 */
public external abstract class SVGRadialGradientElement : SVGGradientElement {
    open konst cx: SVGAnimatedLength
    open konst cy: SVGAnimatedLength
    open konst r: SVGAnimatedLength
    open konst fx: SVGAnimatedLength
    open konst fy: SVGAnimatedLength
    open konst fr: SVGAnimatedLength

    companion object {
        konst SVG_SPREADMETHOD_UNKNOWN: Short
        konst SVG_SPREADMETHOD_PAD: Short
        konst SVG_SPREADMETHOD_REFLECT: Short
        konst SVG_SPREADMETHOD_REPEAT: Short
        konst SVG_UNIT_TYPE_UNKNOWN: Short
        konst SVG_UNIT_TYPE_USERSPACEONUSE: Short
        konst SVG_UNIT_TYPE_OBJECTBOUNDINGBOX: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external abstract class SVGMeshGradientElement : SVGGradientElement {
    companion object {
        konst SVG_SPREADMETHOD_UNKNOWN: Short
        konst SVG_SPREADMETHOD_PAD: Short
        konst SVG_SPREADMETHOD_REFLECT: Short
        konst SVG_SPREADMETHOD_REPEAT: Short
        konst SVG_UNIT_TYPE_UNKNOWN: Short
        konst SVG_UNIT_TYPE_USERSPACEONUSE: Short
        konst SVG_UNIT_TYPE_OBJECTBOUNDINGBOX: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external abstract class SVGMeshrowElement : SVGElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external abstract class SVGMeshpatchElement : SVGElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGStopElement](https://developer.mozilla.org/en/docs/Web/API/SVGStopElement) to Kotlin
 */
public external abstract class SVGStopElement : SVGElement {
    open konst offset: SVGAnimatedNumber

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGPatternElement](https://developer.mozilla.org/en/docs/Web/API/SVGPatternElement) to Kotlin
 */
public external abstract class SVGPatternElement : SVGElement, SVGFitToViewBox, SVGURIReference, SVGUnitTypes {
    open konst patternUnits: SVGAnimatedEnumeration
    open konst patternContentUnits: SVGAnimatedEnumeration
    open konst patternTransform: SVGAnimatedTransformList
    open konst x: SVGAnimatedLength
    open konst y: SVGAnimatedLength
    open konst width: SVGAnimatedLength
    open konst height: SVGAnimatedLength

    companion object {
        konst SVG_UNIT_TYPE_UNKNOWN: Short
        konst SVG_UNIT_TYPE_USERSPACEONUSE: Short
        konst SVG_UNIT_TYPE_OBJECTBOUNDINGBOX: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external abstract class SVGHatchElement : SVGElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

public external abstract class SVGHatchpathElement : SVGElement {
    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGCursorElement](https://developer.mozilla.org/en/docs/Web/API/SVGCursorElement) to Kotlin
 */
public external abstract class SVGCursorElement : SVGElement, SVGURIReference {
    open konst x: SVGAnimatedLength
    open konst y: SVGAnimatedLength

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGScriptElement](https://developer.mozilla.org/en/docs/Web/API/SVGScriptElement) to Kotlin
 */
public external abstract class SVGScriptElement : SVGElement, SVGURIReference, HTMLOrSVGScriptElement {
    open var type: String
    open var crossOrigin: String?

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGAElement](https://developer.mozilla.org/en/docs/Web/API/SVGAElement) to Kotlin
 */
public external abstract class SVGAElement : SVGGraphicsElement, SVGURIReference {
    open konst target: SVGAnimatedString
    open konst download: SVGAnimatedString
    open konst rel: SVGAnimatedString
    open konst relList: SVGAnimatedString
    open konst hreflang: SVGAnimatedString
    open konst type: SVGAnimatedString

    companion object {
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}

/**
 * Exposes the JavaScript [SVGViewElement](https://developer.mozilla.org/en/docs/Web/API/SVGViewElement) to Kotlin
 */
public external abstract class SVGViewElement : SVGElement, SVGFitToViewBox, SVGZoomAndPan {
    companion object {
        konst SVG_ZOOMANDPAN_UNKNOWN: Short
        konst SVG_ZOOMANDPAN_DISABLE: Short
        konst SVG_ZOOMANDPAN_MAGNIFY: Short
        konst ELEMENT_NODE: Short
        konst ATTRIBUTE_NODE: Short
        konst TEXT_NODE: Short
        konst CDATA_SECTION_NODE: Short
        konst ENTITY_REFERENCE_NODE: Short
        konst ENTITY_NODE: Short
        konst PROCESSING_INSTRUCTION_NODE: Short
        konst COMMENT_NODE: Short
        konst DOCUMENT_NODE: Short
        konst DOCUMENT_TYPE_NODE: Short
        konst DOCUMENT_FRAGMENT_NODE: Short
        konst NOTATION_NODE: Short
        konst DOCUMENT_POSITION_DISCONNECTED: Short
        konst DOCUMENT_POSITION_PRECEDING: Short
        konst DOCUMENT_POSITION_FOLLOWING: Short
        konst DOCUMENT_POSITION_CONTAINS: Short
        konst DOCUMENT_POSITION_CONTAINED_BY: Short
        konst DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Short
    }
}