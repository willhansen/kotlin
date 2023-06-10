/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.w3c.dom

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.clipboard.*
import org.w3c.dom.css.*
import org.w3c.dom.encryptedmedia.*
import org.w3c.dom.events.*
import org.w3c.dom.mediacapture.*
import org.w3c.dom.mediasource.*
import org.w3c.dom.pointerevents.*
import org.w3c.dom.svg.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*

public external abstract class HTMLAllCollection : JsAny {
    open konst length: Int
    fun item(nameOrIndex: String = definedExternally): UnionElementOrHTMLCollection?
    fun namedItem(name: String): UnionElementOrHTMLCollection?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForHTMLAllCollection(obj: HTMLAllCollection, index: Int): Element? { js("return obj[index];") }

public operator fun HTMLAllCollection.get(index: Int): Element? = getMethodImplForHTMLAllCollection(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForHTMLAllCollection(obj: HTMLAllCollection, name: String): UnionElementOrHTMLCollection? { js("return obj[name];") }

public operator fun HTMLAllCollection.get(name: String): UnionElementOrHTMLCollection? = getMethodImplForHTMLAllCollection(this, name)

/**
 * Exposes the JavaScript [HTMLFormControlsCollection](https://developer.mozilla.org/en/docs/Web/API/HTMLFormControlsCollection) to Kotlin
 */
public external abstract class HTMLFormControlsCollection : HTMLCollection, JsAny

/**
 * Exposes the JavaScript [RadioNodeList](https://developer.mozilla.org/en/docs/Web/API/RadioNodeList) to Kotlin
 */
public external abstract class RadioNodeList : NodeList, UnionElementOrRadioNodeList, JsAny {
    open var konstue: String
}

/**
 * Exposes the JavaScript [HTMLOptionsCollection](https://developer.mozilla.org/en/docs/Web/API/HTMLOptionsCollection) to Kotlin
 */
public external abstract class HTMLOptionsCollection : HTMLCollection, JsAny {
    override var length: Int
    open var selectedIndex: Int
    fun add(element: UnionHTMLOptGroupElementOrHTMLOptionElement, before: JsAny? = definedExternally)
    fun remove(index: Int)
}

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForHTMLOptionsCollection(obj: HTMLOptionsCollection, index: Int, option: HTMLOptionElement?) { js("obj[index] = option;") }

public operator fun HTMLOptionsCollection.set(index: Int, option: HTMLOptionElement?) = setMethodImplForHTMLOptionsCollection(this, index, option)

/**
 * Exposes the JavaScript [HTMLElement](https://developer.mozilla.org/en/docs/Web/API/HTMLElement) to Kotlin
 */
public external abstract class HTMLElement : Element, GlobalEventHandlers, DocumentAndElementEventHandlers, ElementContentEditable, ElementCSSInlineStyle, JsAny {
    open var title: String
    open var lang: String
    open var translate: Boolean
    open var dir: String
    open konst dataset: DOMStringMap
    open var hidden: Boolean
    open var tabIndex: Int
    open var accessKey: String
    open konst accessKeyLabel: String
    open var draggable: Boolean
    open konst dropzone: DOMTokenList
    open var contextMenu: HTMLMenuElement?
    open var spellcheck: Boolean
    open var innerText: String
    open konst offsetParent: Element?
    open konst offsetTop: Int
    open konst offsetLeft: Int
    open konst offsetWidth: Int
    open konst offsetHeight: Int
    fun click()
    fun focus()
    fun blur()
    fun forceSpellCheck()

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
 * Exposes the JavaScript [HTMLUnknownElement](https://developer.mozilla.org/en/docs/Web/API/HTMLUnknownElement) to Kotlin
 */
public external abstract class HTMLUnknownElement : HTMLElement, JsAny {
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
 * Exposes the JavaScript [DOMStringMap](https://developer.mozilla.org/en/docs/Web/API/DOMStringMap) to Kotlin
 */
public external abstract class DOMStringMap : JsAny

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForDOMStringMap(obj: DOMStringMap, name: String): String? { js("return obj[name];") }

public operator fun DOMStringMap.get(name: String): String? = getMethodImplForDOMStringMap(this, name)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForDOMStringMap(obj: DOMStringMap, name: String, konstue: String) { js("obj[name] = konstue;") }

public operator fun DOMStringMap.set(name: String, konstue: String) = setMethodImplForDOMStringMap(this, name, konstue)

/**
 * Exposes the JavaScript [HTMLHtmlElement](https://developer.mozilla.org/en/docs/Web/API/HTMLHtmlElement) to Kotlin
 */
public external abstract class HTMLHtmlElement : HTMLElement, JsAny {
    open var version: String

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
 * Exposes the JavaScript [HTMLHeadElement](https://developer.mozilla.org/en/docs/Web/API/HTMLHeadElement) to Kotlin
 */
public external abstract class HTMLHeadElement : HTMLElement, JsAny {
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
 * Exposes the JavaScript [HTMLTitleElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTitleElement) to Kotlin
 */
public external abstract class HTMLTitleElement : HTMLElement, JsAny {
    open var text: String

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
 * Exposes the JavaScript [HTMLBaseElement](https://developer.mozilla.org/en/docs/Web/API/HTMLBaseElement) to Kotlin
 */
public external abstract class HTMLBaseElement : HTMLElement, JsAny {
    open var href: String
    open var target: String

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
 * Exposes the JavaScript [HTMLLinkElement](https://developer.mozilla.org/en/docs/Web/API/HTMLLinkElement) to Kotlin
 */
public external abstract class HTMLLinkElement : HTMLElement, LinkStyle, JsAny {
    open var href: String
    open var crossOrigin: String?
    open var rel: String
    open var `as`: RequestDestination
    open konst relList: DOMTokenList
    open var media: String
    open var nonce: String
    open var hreflang: String
    open var type: String
    open konst sizes: DOMTokenList
    open var referrerPolicy: String
    open var charset: String
    open var rev: String
    open var target: String
    open var scope: String
    open var workerType: WorkerType

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
 * Exposes the JavaScript [HTMLMetaElement](https://developer.mozilla.org/en/docs/Web/API/HTMLMetaElement) to Kotlin
 */
public external abstract class HTMLMetaElement : HTMLElement, JsAny {
    open var name: String
    open var httpEquiv: String
    open var content: String
    open var scheme: String

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
 * Exposes the JavaScript [HTMLStyleElement](https://developer.mozilla.org/en/docs/Web/API/HTMLStyleElement) to Kotlin
 */
public external abstract class HTMLStyleElement : HTMLElement, LinkStyle, JsAny {
    open var media: String
    open var nonce: String
    open var type: String

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
 * Exposes the JavaScript [HTMLBodyElement](https://developer.mozilla.org/en/docs/Web/API/HTMLBodyElement) to Kotlin
 */
public external abstract class HTMLBodyElement : HTMLElement, WindowEventHandlers, JsAny {
    open var text: String
    open var link: String
    open var vLink: String
    open var aLink: String
    open var bgColor: String
    open var background: String

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
 * Exposes the JavaScript [HTMLHeadingElement](https://developer.mozilla.org/en/docs/Web/API/HTMLHeadingElement) to Kotlin
 */
public external abstract class HTMLHeadingElement : HTMLElement, JsAny {
    open var align: String

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
 * Exposes the JavaScript [HTMLParagraphElement](https://developer.mozilla.org/en/docs/Web/API/HTMLParagraphElement) to Kotlin
 */
public external abstract class HTMLParagraphElement : HTMLElement, JsAny {
    open var align: String

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
 * Exposes the JavaScript [HTMLHRElement](https://developer.mozilla.org/en/docs/Web/API/HTMLHRElement) to Kotlin
 */
public external abstract class HTMLHRElement : HTMLElement, JsAny {
    open var align: String
    open var color: String
    open var noShade: Boolean
    open var size: String
    open var width: String

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
 * Exposes the JavaScript [HTMLPreElement](https://developer.mozilla.org/en/docs/Web/API/HTMLPreElement) to Kotlin
 */
public external abstract class HTMLPreElement : HTMLElement, JsAny {
    open var width: Int

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
 * Exposes the JavaScript [HTMLQuoteElement](https://developer.mozilla.org/en/docs/Web/API/HTMLQuoteElement) to Kotlin
 */
public external abstract class HTMLQuoteElement : HTMLElement, JsAny {
    open var cite: String

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
 * Exposes the JavaScript [HTMLOListElement](https://developer.mozilla.org/en/docs/Web/API/HTMLOListElement) to Kotlin
 */
public external abstract class HTMLOListElement : HTMLElement, JsAny {
    open var reversed: Boolean
    open var start: Int
    open var type: String
    open var compact: Boolean

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
 * Exposes the JavaScript [HTMLUListElement](https://developer.mozilla.org/en/docs/Web/API/HTMLUListElement) to Kotlin
 */
public external abstract class HTMLUListElement : HTMLElement, JsAny {
    open var compact: Boolean
    open var type: String

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
 * Exposes the JavaScript [HTMLLIElement](https://developer.mozilla.org/en/docs/Web/API/HTMLLIElement) to Kotlin
 */
public external abstract class HTMLLIElement : HTMLElement, JsAny {
    open var konstue: Int
    open var type: String

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
 * Exposes the JavaScript [HTMLDListElement](https://developer.mozilla.org/en/docs/Web/API/HTMLDListElement) to Kotlin
 */
public external abstract class HTMLDListElement : HTMLElement, JsAny {
    open var compact: Boolean

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
 * Exposes the JavaScript [HTMLDivElement](https://developer.mozilla.org/en/docs/Web/API/HTMLDivElement) to Kotlin
 */
public external abstract class HTMLDivElement : HTMLElement, JsAny {
    open var align: String

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
 * Exposes the JavaScript [HTMLAnchorElement](https://developer.mozilla.org/en/docs/Web/API/HTMLAnchorElement) to Kotlin
 */
public external abstract class HTMLAnchorElement : HTMLElement, HTMLHyperlinkElementUtils, JsAny {
    open var target: String
    open var download: String
    open var ping: String
    open var rel: String
    open konst relList: DOMTokenList
    open var hreflang: String
    open var type: String
    open var text: String
    open var referrerPolicy: String
    open var coords: String
    open var charset: String
    open var name: String
    open var rev: String
    open var shape: String

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
 * Exposes the JavaScript [HTMLDataElement](https://developer.mozilla.org/en/docs/Web/API/HTMLDataElement) to Kotlin
 */
public external abstract class HTMLDataElement : HTMLElement, JsAny {
    open var konstue: String

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
 * Exposes the JavaScript [HTMLTimeElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTimeElement) to Kotlin
 */
public external abstract class HTMLTimeElement : HTMLElement, JsAny {
    open var dateTime: String

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
 * Exposes the JavaScript [HTMLSpanElement](https://developer.mozilla.org/en/docs/Web/API/HTMLSpanElement) to Kotlin
 */
public external abstract class HTMLSpanElement : HTMLElement, JsAny {
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
 * Exposes the JavaScript [HTMLBRElement](https://developer.mozilla.org/en/docs/Web/API/HTMLBRElement) to Kotlin
 */
public external abstract class HTMLBRElement : HTMLElement, JsAny {
    open var clear: String

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
 * Exposes the JavaScript [HTMLHyperlinkElementUtils](https://developer.mozilla.org/en/docs/Web/API/HTMLHyperlinkElementUtils) to Kotlin
 */
public external interface HTMLHyperlinkElementUtils : JsAny {
    var href: String
    konst origin: String
    var protocol: String
    var username: String
    var password: String
    var host: String
    var hostname: String
    var port: String
    var pathname: String
    var search: String
    var hash: String
}

/**
 * Exposes the JavaScript [HTMLModElement](https://developer.mozilla.org/en/docs/Web/API/HTMLModElement) to Kotlin
 */
public external abstract class HTMLModElement : HTMLElement, JsAny {
    open var cite: String
    open var dateTime: String

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
 * Exposes the JavaScript [HTMLPictureElement](https://developer.mozilla.org/en/docs/Web/API/HTMLPictureElement) to Kotlin
 */
public external abstract class HTMLPictureElement : HTMLElement, JsAny {
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
 * Exposes the JavaScript [HTMLSourceElement](https://developer.mozilla.org/en/docs/Web/API/HTMLSourceElement) to Kotlin
 */
public external abstract class HTMLSourceElement : HTMLElement, JsAny {
    open var src: String
    open var type: String
    open var srcset: String
    open var sizes: String
    open var media: String

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
 * Exposes the JavaScript [HTMLImageElement](https://developer.mozilla.org/en/docs/Web/API/HTMLImageElement) to Kotlin
 */
public external abstract class HTMLImageElement : HTMLElement, HTMLOrSVGImageElement, TexImageSource, JsAny {
    open var alt: String
    open var src: String
    open var srcset: String
    open var sizes: String
    open var crossOrigin: String?
    open var useMap: String
    open var isMap: Boolean
    open var width: Int
    open var height: Int
    open konst naturalWidth: Int
    open konst naturalHeight: Int
    open konst complete: Boolean
    open konst currentSrc: String
    open var referrerPolicy: String
    open var name: String
    open var lowsrc: String
    open var align: String
    open var hspace: Int
    open var vspace: Int
    open var longDesc: String
    open var border: String
    open konst x: Int
    open konst y: Int

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
 * Exposes the JavaScript [HTMLIFrameElement](https://developer.mozilla.org/en/docs/Web/API/HTMLIFrameElement) to Kotlin
 */
public external abstract class HTMLIFrameElement : HTMLElement, JsAny {
    open var src: String
    open var srcdoc: String
    open var name: String
    open konst sandbox: DOMTokenList
    open var allowFullscreen: Boolean
    open var allowUserMedia: Boolean
    open var width: String
    open var height: String
    open var referrerPolicy: String
    open konst contentDocument: Document?
    open konst contentWindow: Window?
    open var align: String
    open var scrolling: String
    open var frameBorder: String
    open var longDesc: String
    open var marginHeight: String
    open var marginWidth: String
    fun getSVGDocument(): Document?

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
 * Exposes the JavaScript [HTMLEmbedElement](https://developer.mozilla.org/en/docs/Web/API/HTMLEmbedElement) to Kotlin
 */
public external abstract class HTMLEmbedElement : HTMLElement, JsAny {
    open var src: String
    open var type: String
    open var width: String
    open var height: String
    open var align: String
    open var name: String
    fun getSVGDocument(): Document?

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
 * Exposes the JavaScript [HTMLObjectElement](https://developer.mozilla.org/en/docs/Web/API/HTMLObjectElement) to Kotlin
 */
public external abstract class HTMLObjectElement : HTMLElement, JsAny {
    open var data: String
    open var type: String
    open var typeMustMatch: Boolean
    open var name: String
    open var useMap: String
    open konst form: HTMLFormElement?
    open var width: String
    open var height: String
    open konst contentDocument: Document?
    open konst contentWindow: Window?
    open konst willValidate: Boolean
    open konst konstidity: ValidityState
    open konst konstidationMessage: String
    open var align: String
    open var archive: String
    open var code: String
    open var declare: Boolean
    open var hspace: Int
    open var standby: String
    open var vspace: Int
    open var codeBase: String
    open var codeType: String
    open var border: String
    fun getSVGDocument(): Document?
    fun checkValidity(): Boolean
    fun reportValidity(): Boolean
    fun setCustomValidity(error: String)

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
 * Exposes the JavaScript [HTMLParamElement](https://developer.mozilla.org/en/docs/Web/API/HTMLParamElement) to Kotlin
 */
public external abstract class HTMLParamElement : HTMLElement, JsAny {
    open var name: String
    open var konstue: String
    open var type: String
    open var konstueType: String

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
 * Exposes the JavaScript [HTMLVideoElement](https://developer.mozilla.org/en/docs/Web/API/HTMLVideoElement) to Kotlin
 */
public external abstract class HTMLVideoElement : HTMLMediaElement, CanvasImageSource, TexImageSource, JsAny {
    open var width: Int
    open var height: Int
    open konst videoWidth: Int
    open konst videoHeight: Int
    open var poster: String
    open var playsInline: Boolean

    companion object {
        konst NETWORK_EMPTY: Short
        konst NETWORK_IDLE: Short
        konst NETWORK_LOADING: Short
        konst NETWORK_NO_SOURCE: Short
        konst HAVE_NOTHING: Short
        konst HAVE_METADATA: Short
        konst HAVE_CURRENT_DATA: Short
        konst HAVE_FUTURE_DATA: Short
        konst HAVE_ENOUGH_DATA: Short
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
 * Exposes the JavaScript [HTMLAudioElement](https://developer.mozilla.org/en/docs/Web/API/HTMLAudioElement) to Kotlin
 */
public external abstract class HTMLAudioElement : HTMLMediaElement, JsAny {
    companion object {
        konst NETWORK_EMPTY: Short
        konst NETWORK_IDLE: Short
        konst NETWORK_LOADING: Short
        konst NETWORK_NO_SOURCE: Short
        konst HAVE_NOTHING: Short
        konst HAVE_METADATA: Short
        konst HAVE_CURRENT_DATA: Short
        konst HAVE_FUTURE_DATA: Short
        konst HAVE_ENOUGH_DATA: Short
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
 * Exposes the JavaScript [HTMLTrackElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTrackElement) to Kotlin
 */
public external abstract class HTMLTrackElement : HTMLElement, JsAny {
    open var kind: String
    open var src: String
    open var srclang: String
    open var label: String
    open var default: Boolean
    open konst readyState: Short
    open konst track: TextTrack

    companion object {
        konst NONE: Short
        konst LOADING: Short
        konst LOADED: Short
        konst ERROR: Short
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
 * Exposes the JavaScript [HTMLMediaElement](https://developer.mozilla.org/en/docs/Web/API/HTMLMediaElement) to Kotlin
 */
public external abstract class HTMLMediaElement : HTMLElement, JsAny {
    open konst error: MediaError?
    open var src: String
    open var srcObject: MediaProvider?
    open konst currentSrc: String
    open var crossOrigin: String?
    open konst networkState: Short
    open var preload: String
    open konst buffered: TimeRanges
    open konst readyState: Short
    open konst seeking: Boolean
    open var currentTime: Double
    open konst duration: Double
    open konst paused: Boolean
    open var defaultPlaybackRate: Double
    open var playbackRate: Double
    open konst played: TimeRanges
    open konst seekable: TimeRanges
    open konst ended: Boolean
    open var autoplay: Boolean
    open var loop: Boolean
    open var controls: Boolean
    open var volume: Double
    open var muted: Boolean
    open var defaultMuted: Boolean
    open konst audioTracks: AudioTrackList
    open konst videoTracks: VideoTrackList
    open konst textTracks: TextTrackList
    open konst mediaKeys: MediaKeys?
    open var onencrypted: ((Event) -> JsAny?)?
    open var onwaitingforkey: ((Event) -> JsAny?)?
    fun load()
    fun canPlayType(type: String): CanPlayTypeResult
    fun fastSeek(time: Double)
    fun getStartDate(): JsAny
    fun play(): Promise<Nothing?>
    fun pause()
    fun addTextTrack(kind: TextTrackKind, label: String = definedExternally, language: String = definedExternally): TextTrack
    fun setMediaKeys(mediaKeys: MediaKeys?): Promise<Nothing?>

    companion object {
        konst NETWORK_EMPTY: Short
        konst NETWORK_IDLE: Short
        konst NETWORK_LOADING: Short
        konst NETWORK_NO_SOURCE: Short
        konst HAVE_NOTHING: Short
        konst HAVE_METADATA: Short
        konst HAVE_CURRENT_DATA: Short
        konst HAVE_FUTURE_DATA: Short
        konst HAVE_ENOUGH_DATA: Short
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
 * Exposes the JavaScript [MediaError](https://developer.mozilla.org/en/docs/Web/API/MediaError) to Kotlin
 */
public external abstract class MediaError : JsAny {
    open konst code: Short

    companion object {
        konst MEDIA_ERR_ABORTED: Short
        konst MEDIA_ERR_NETWORK: Short
        konst MEDIA_ERR_DECODE: Short
        konst MEDIA_ERR_SRC_NOT_SUPPORTED: Short
    }
}

/**
 * Exposes the JavaScript [AudioTrackList](https://developer.mozilla.org/en/docs/Web/API/AudioTrackList) to Kotlin
 */
public external abstract class AudioTrackList : EventTarget, JsAny {
    open konst length: Int
    open var onchange: ((Event) -> JsAny?)?
    open var onaddtrack: ((TrackEvent) -> JsAny?)?
    open var onremovetrack: ((TrackEvent) -> JsAny?)?
    fun getTrackById(id: String): AudioTrack?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForAudioTrackList(obj: AudioTrackList, index: Int): AudioTrack? { js("return obj[index];") }

public operator fun AudioTrackList.get(index: Int): AudioTrack? = getMethodImplForAudioTrackList(this, index)

/**
 * Exposes the JavaScript [AudioTrack](https://developer.mozilla.org/en/docs/Web/API/AudioTrack) to Kotlin
 */
public external abstract class AudioTrack : UnionAudioTrackOrTextTrackOrVideoTrack, JsAny {
    open konst id: String
    open konst kind: String
    open konst label: String
    open konst language: String
    open var enabled: Boolean
    open konst sourceBuffer: SourceBuffer?
}

/**
 * Exposes the JavaScript [VideoTrackList](https://developer.mozilla.org/en/docs/Web/API/VideoTrackList) to Kotlin
 */
public external abstract class VideoTrackList : EventTarget, JsAny {
    open konst length: Int
    open konst selectedIndex: Int
    open var onchange: ((Event) -> JsAny?)?
    open var onaddtrack: ((TrackEvent) -> JsAny?)?
    open var onremovetrack: ((TrackEvent) -> JsAny?)?
    fun getTrackById(id: String): VideoTrack?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForVideoTrackList(obj: VideoTrackList, index: Int): VideoTrack? { js("return obj[index];") }

public operator fun VideoTrackList.get(index: Int): VideoTrack? = getMethodImplForVideoTrackList(this, index)

/**
 * Exposes the JavaScript [VideoTrack](https://developer.mozilla.org/en/docs/Web/API/VideoTrack) to Kotlin
 */
public external abstract class VideoTrack : UnionAudioTrackOrTextTrackOrVideoTrack, JsAny {
    open konst id: String
    open konst kind: String
    open konst label: String
    open konst language: String
    open var selected: Boolean
    open konst sourceBuffer: SourceBuffer?
}

public external abstract class TextTrackList : EventTarget, JsAny {
    open konst length: Int
    open var onchange: ((Event) -> JsAny?)?
    open var onaddtrack: ((TrackEvent) -> JsAny?)?
    open var onremovetrack: ((TrackEvent) -> JsAny?)?
    fun getTrackById(id: String): TextTrack?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForTextTrackList(obj: TextTrackList, index: Int): TextTrack? { js("return obj[index];") }

public operator fun TextTrackList.get(index: Int): TextTrack? = getMethodImplForTextTrackList(this, index)

/**
 * Exposes the JavaScript [TextTrack](https://developer.mozilla.org/en/docs/Web/API/TextTrack) to Kotlin
 */
public external abstract class TextTrack : EventTarget, UnionAudioTrackOrTextTrackOrVideoTrack, JsAny {
    open konst kind: TextTrackKind
    open konst label: String
    open konst language: String
    open konst id: String
    open konst inBandMetadataTrackDispatchType: String
    open var mode: TextTrackMode
    open konst cues: TextTrackCueList?
    open konst activeCues: TextTrackCueList?
    open var oncuechange: ((Event) -> JsAny?)?
    open konst sourceBuffer: SourceBuffer?
    fun addCue(cue: TextTrackCue)
    fun removeCue(cue: TextTrackCue)
}

public external abstract class TextTrackCueList : JsAny {
    open konst length: Int
    fun getCueById(id: String): TextTrackCue?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForTextTrackCueList(obj: TextTrackCueList, index: Int): TextTrackCue? { js("return obj[index];") }

public operator fun TextTrackCueList.get(index: Int): TextTrackCue? = getMethodImplForTextTrackCueList(this, index)

/**
 * Exposes the JavaScript [TextTrackCue](https://developer.mozilla.org/en/docs/Web/API/TextTrackCue) to Kotlin
 */
public external abstract class TextTrackCue : EventTarget, JsAny {
    open konst track: TextTrack?
    open var id: String
    open var startTime: Double
    open var endTime: Double
    open var pauseOnExit: Boolean
    open var onenter: ((Event) -> JsAny?)?
    open var onexit: ((Event) -> JsAny?)?
}

/**
 * Exposes the JavaScript [TimeRanges](https://developer.mozilla.org/en/docs/Web/API/TimeRanges) to Kotlin
 */
public external abstract class TimeRanges : JsAny {
    open konst length: Int
    fun start(index: Int): Double
    fun end(index: Int): Double
}

/**
 * Exposes the JavaScript [TrackEvent](https://developer.mozilla.org/en/docs/Web/API/TrackEvent) to Kotlin
 */
public external open class TrackEvent(type: String, eventInitDict: TrackEventInit = definedExternally) : Event, JsAny {
    open konst track: UnionAudioTrackOrTextTrackOrVideoTrack?

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface TrackEventInit : EventInit, JsAny {
    var track: UnionAudioTrackOrTextTrackOrVideoTrack? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun TrackEventInit(track: UnionAudioTrackOrTextTrackOrVideoTrack? = null, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): TrackEventInit { js("return { track, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [HTMLMapElement](https://developer.mozilla.org/en/docs/Web/API/HTMLMapElement) to Kotlin
 */
public external abstract class HTMLMapElement : HTMLElement, JsAny {
    open var name: String
    open konst areas: HTMLCollection

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
 * Exposes the JavaScript [HTMLAreaElement](https://developer.mozilla.org/en/docs/Web/API/HTMLAreaElement) to Kotlin
 */
public external abstract class HTMLAreaElement : HTMLElement, HTMLHyperlinkElementUtils, JsAny {
    open var alt: String
    open var coords: String
    open var shape: String
    open var target: String
    open var download: String
    open var ping: String
    open var rel: String
    open konst relList: DOMTokenList
    open var referrerPolicy: String
    open var noHref: Boolean

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
 * Exposes the JavaScript [HTMLTableElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTableElement) to Kotlin
 */
public external abstract class HTMLTableElement : HTMLElement, JsAny {
    open var caption: HTMLTableCaptionElement?
    open var tHead: HTMLTableSectionElement?
    open var tFoot: HTMLTableSectionElement?
    open konst tBodies: HTMLCollection
    open konst rows: HTMLCollection
    open var align: String
    open var border: String
    open var frame: String
    open var rules: String
    open var summary: String
    open var width: String
    open var bgColor: String
    open var cellPadding: String
    open var cellSpacing: String
    fun createCaption(): HTMLTableCaptionElement
    fun deleteCaption()
    fun createTHead(): HTMLTableSectionElement
    fun deleteTHead()
    fun createTFoot(): HTMLTableSectionElement
    fun deleteTFoot()
    fun createTBody(): HTMLTableSectionElement
    fun insertRow(index: Int = definedExternally): HTMLTableRowElement
    fun deleteRow(index: Int)

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
 * Exposes the JavaScript [HTMLTableCaptionElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTableCaptionElement) to Kotlin
 */
public external abstract class HTMLTableCaptionElement : HTMLElement, JsAny {
    open var align: String

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
 * Exposes the JavaScript [HTMLTableColElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTableColElement) to Kotlin
 */
public external abstract class HTMLTableColElement : HTMLElement, JsAny {
    open var span: Int
    open var align: String
    open var ch: String
    open var chOff: String
    open var vAlign: String
    open var width: String

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
 * Exposes the JavaScript [HTMLTableSectionElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTableSectionElement) to Kotlin
 */
public external abstract class HTMLTableSectionElement : HTMLElement, JsAny {
    open konst rows: HTMLCollection
    open var align: String
    open var ch: String
    open var chOff: String
    open var vAlign: String
    fun insertRow(index: Int = definedExternally): HTMLElement
    fun deleteRow(index: Int)

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
 * Exposes the JavaScript [HTMLTableRowElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTableRowElement) to Kotlin
 */
public external abstract class HTMLTableRowElement : HTMLElement, JsAny {
    open konst rowIndex: Int
    open konst sectionRowIndex: Int
    open konst cells: HTMLCollection
    open var align: String
    open var ch: String
    open var chOff: String
    open var vAlign: String
    open var bgColor: String
    fun insertCell(index: Int = definedExternally): HTMLElement
    fun deleteCell(index: Int)

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
 * Exposes the JavaScript [HTMLTableCellElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTableCellElement) to Kotlin
 */
public external abstract class HTMLTableCellElement : HTMLElement, JsAny {
    open var colSpan: Int
    open var rowSpan: Int
    open var headers: String
    open konst cellIndex: Int
    open var scope: String
    open var abbr: String
    open var align: String
    open var axis: String
    open var height: String
    open var width: String
    open var ch: String
    open var chOff: String
    open var noWrap: Boolean
    open var vAlign: String
    open var bgColor: String

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
 * Exposes the JavaScript [HTMLFormElement](https://developer.mozilla.org/en/docs/Web/API/HTMLFormElement) to Kotlin
 */
public external abstract class HTMLFormElement : HTMLElement, JsAny {
    open var acceptCharset: String
    open var action: String
    open var autocomplete: String
    open var enctype: String
    open var encoding: String
    open var method: String
    open var name: String
    open var noValidate: Boolean
    open var target: String
    open konst elements: HTMLFormControlsCollection
    open konst length: Int
    fun submit()
    fun reset()
    fun checkValidity(): Boolean
    fun reportValidity(): Boolean

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

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForHTMLFormElement(obj: HTMLFormElement, index: Int): Element? { js("return obj[index];") }

public operator fun HTMLFormElement.get(index: Int): Element? = getMethodImplForHTMLFormElement(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForHTMLFormElement(obj: HTMLFormElement, name: String): UnionElementOrRadioNodeList? { js("return obj[name];") }

public operator fun HTMLFormElement.get(name: String): UnionElementOrRadioNodeList? = getMethodImplForHTMLFormElement(this, name)

/**
 * Exposes the JavaScript [HTMLLabelElement](https://developer.mozilla.org/en/docs/Web/API/HTMLLabelElement) to Kotlin
 */
public external abstract class HTMLLabelElement : HTMLElement, JsAny {
    open konst form: HTMLFormElement?
    open var htmlFor: String
    open konst control: HTMLElement?

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
 * Exposes the JavaScript [HTMLInputElement](https://developer.mozilla.org/en/docs/Web/API/HTMLInputElement) to Kotlin
 */
public external abstract class HTMLInputElement : HTMLElement, JsAny {
    open var accept: String
    open var alt: String
    open var autocomplete: String
    open var autofocus: Boolean
    open var defaultChecked: Boolean
    open var checked: Boolean
    open var dirName: String
    open var disabled: Boolean
    open konst form: HTMLFormElement?
    open konst files: FileList?
    open var formAction: String
    open var formEnctype: String
    open var formMethod: String
    open var formNoValidate: Boolean
    open var formTarget: String
    open var height: Int
    open var indeterminate: Boolean
    open var inputMode: String
    open konst list: HTMLElement?
    open var max: String
    open var maxLength: Int
    open var min: String
    open var minLength: Int
    open var multiple: Boolean
    open var name: String
    open var pattern: String
    open var placeholder: String
    open var readOnly: Boolean
    open var required: Boolean
    open var size: Int
    open var src: String
    open var step: String
    open var type: String
    open var defaultValue: String
    open var konstue: String
    open var konstueAsDate: JsAny?
    open var konstueAsNumber: Double
    open var width: Int
    open konst willValidate: Boolean
    open konst konstidity: ValidityState
    open konst konstidationMessage: String
    open konst labels: NodeList
    open var selectionStart: Int?
    open var selectionEnd: Int?
    open var selectionDirection: String?
    open var align: String
    open var useMap: String
    fun stepUp(n: Int = definedExternally)
    fun stepDown(n: Int = definedExternally)
    fun checkValidity(): Boolean
    fun reportValidity(): Boolean
    fun setCustomValidity(error: String)
    fun select()
    fun setRangeText(replacement: String)
    fun setRangeText(replacement: String, start: Int, end: Int, selectionMode: SelectionMode = definedExternally)
    fun setSelectionRange(start: Int, end: Int, direction: String = definedExternally)

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
 * Exposes the JavaScript [HTMLButtonElement](https://developer.mozilla.org/en/docs/Web/API/HTMLButtonElement) to Kotlin
 */
public external abstract class HTMLButtonElement : HTMLElement, JsAny {
    open var autofocus: Boolean
    open var disabled: Boolean
    open konst form: HTMLFormElement?
    open var formAction: String
    open var formEnctype: String
    open var formMethod: String
    open var formNoValidate: Boolean
    open var formTarget: String
    open var name: String
    open var type: String
    open var konstue: String
    open var menu: HTMLMenuElement?
    open konst willValidate: Boolean
    open konst konstidity: ValidityState
    open konst konstidationMessage: String
    open konst labels: NodeList
    fun checkValidity(): Boolean
    fun reportValidity(): Boolean
    fun setCustomValidity(error: String)

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
 * Exposes the JavaScript [HTMLSelectElement](https://developer.mozilla.org/en/docs/Web/API/HTMLSelectElement) to Kotlin
 */
public external abstract class HTMLSelectElement : HTMLElement, ItemArrayLike<Element>, JsAny {
    open var autocomplete: String
    open var autofocus: Boolean
    open var disabled: Boolean
    open konst form: HTMLFormElement?
    open var multiple: Boolean
    open var name: String
    open var required: Boolean
    open var size: Int
    open konst type: String
    open konst options: HTMLOptionsCollection
    override var length: Int
    open konst selectedOptions: HTMLCollection
    open var selectedIndex: Int
    open var konstue: String
    open konst willValidate: Boolean
    open konst konstidity: ValidityState
    open konst konstidationMessage: String
    open konst labels: NodeList
    fun namedItem(name: String): HTMLOptionElement?
    fun add(element: UnionHTMLOptGroupElementOrHTMLOptionElement, before: JsAny? = definedExternally)
    fun remove(index: Int)
    fun checkValidity(): Boolean
    fun reportValidity(): Boolean
    fun setCustomValidity(error: String)
    override fun item(index: Int): Element?

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

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForHTMLSelectElement(obj: HTMLSelectElement, index: Int): Element? { js("return obj[index];") }

public operator fun HTMLSelectElement.get(index: Int): Element? = getMethodImplForHTMLSelectElement(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForHTMLSelectElement(obj: HTMLSelectElement, index: Int, option: HTMLOptionElement?) { js("obj[index] = option;") }

public operator fun HTMLSelectElement.set(index: Int, option: HTMLOptionElement?) = setMethodImplForHTMLSelectElement(this, index, option)

/**
 * Exposes the JavaScript [HTMLDataListElement](https://developer.mozilla.org/en/docs/Web/API/HTMLDataListElement) to Kotlin
 */
public external abstract class HTMLDataListElement : HTMLElement, JsAny {
    open konst options: HTMLCollection

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
 * Exposes the JavaScript [HTMLOptGroupElement](https://developer.mozilla.org/en/docs/Web/API/HTMLOptGroupElement) to Kotlin
 */
public external abstract class HTMLOptGroupElement : HTMLElement, UnionHTMLOptGroupElementOrHTMLOptionElement, JsAny {
    open var disabled: Boolean
    open var label: String

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
 * Exposes the JavaScript [HTMLOptionElement](https://developer.mozilla.org/en/docs/Web/API/HTMLOptionElement) to Kotlin
 */
public external abstract class HTMLOptionElement : HTMLElement, UnionHTMLOptGroupElementOrHTMLOptionElement, JsAny {
    open var disabled: Boolean
    open konst form: HTMLFormElement?
    open var label: String
    open var defaultSelected: Boolean
    open var selected: Boolean
    open var konstue: String
    open var text: String
    open konst index: Int

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
 * Exposes the JavaScript [HTMLTextAreaElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTextAreaElement) to Kotlin
 */
public external abstract class HTMLTextAreaElement : HTMLElement, JsAny {
    open var autocomplete: String
    open var autofocus: Boolean
    open var cols: Int
    open var dirName: String
    open var disabled: Boolean
    open konst form: HTMLFormElement?
    open var inputMode: String
    open var maxLength: Int
    open var minLength: Int
    open var name: String
    open var placeholder: String
    open var readOnly: Boolean
    open var required: Boolean
    open var rows: Int
    open var wrap: String
    open konst type: String
    open var defaultValue: String
    open var konstue: String
    open konst textLength: Int
    open konst willValidate: Boolean
    open konst konstidity: ValidityState
    open konst konstidationMessage: String
    open konst labels: NodeList
    open var selectionStart: Int?
    open var selectionEnd: Int?
    open var selectionDirection: String?
    fun checkValidity(): Boolean
    fun reportValidity(): Boolean
    fun setCustomValidity(error: String)
    fun select()
    fun setRangeText(replacement: String)
    fun setRangeText(replacement: String, start: Int, end: Int, selectionMode: SelectionMode = definedExternally)
    fun setSelectionRange(start: Int, end: Int, direction: String = definedExternally)

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
 * Exposes the JavaScript [HTMLKeygenElement](https://developer.mozilla.org/en/docs/Web/API/HTMLKeygenElement) to Kotlin
 */
public external abstract class HTMLKeygenElement : HTMLElement, JsAny {
    open var autofocus: Boolean
    open var challenge: String
    open var disabled: Boolean
    open konst form: HTMLFormElement?
    open var keytype: String
    open var name: String
    open konst type: String
    open konst willValidate: Boolean
    open konst konstidity: ValidityState
    open konst konstidationMessage: String
    open konst labels: NodeList
    fun checkValidity(): Boolean
    fun reportValidity(): Boolean
    fun setCustomValidity(error: String)

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
 * Exposes the JavaScript [HTMLOutputElement](https://developer.mozilla.org/en/docs/Web/API/HTMLOutputElement) to Kotlin
 */
public external abstract class HTMLOutputElement : HTMLElement, JsAny {
    open konst htmlFor: DOMTokenList
    open konst form: HTMLFormElement?
    open var name: String
    open konst type: String
    open var defaultValue: String
    open var konstue: String
    open konst willValidate: Boolean
    open konst konstidity: ValidityState
    open konst konstidationMessage: String
    open konst labels: NodeList
    fun checkValidity(): Boolean
    fun reportValidity(): Boolean
    fun setCustomValidity(error: String)

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
 * Exposes the JavaScript [HTMLProgressElement](https://developer.mozilla.org/en/docs/Web/API/HTMLProgressElement) to Kotlin
 */
public external abstract class HTMLProgressElement : HTMLElement, JsAny {
    open var konstue: Double
    open var max: Double
    open konst position: Double
    open konst labels: NodeList

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
 * Exposes the JavaScript [HTMLMeterElement](https://developer.mozilla.org/en/docs/Web/API/HTMLMeterElement) to Kotlin
 */
public external abstract class HTMLMeterElement : HTMLElement, JsAny {
    open var konstue: Double
    open var min: Double
    open var max: Double
    open var low: Double
    open var high: Double
    open var optimum: Double
    open konst labels: NodeList

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
 * Exposes the JavaScript [HTMLFieldSetElement](https://developer.mozilla.org/en/docs/Web/API/HTMLFieldSetElement) to Kotlin
 */
public external abstract class HTMLFieldSetElement : HTMLElement, JsAny {
    open var disabled: Boolean
    open konst form: HTMLFormElement?
    open var name: String
    open konst type: String
    open konst elements: HTMLCollection
    open konst willValidate: Boolean
    open konst konstidity: ValidityState
    open konst konstidationMessage: String
    fun checkValidity(): Boolean
    fun reportValidity(): Boolean
    fun setCustomValidity(error: String)

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
 * Exposes the JavaScript [HTMLLegendElement](https://developer.mozilla.org/en/docs/Web/API/HTMLLegendElement) to Kotlin
 */
public external abstract class HTMLLegendElement : HTMLElement, JsAny {
    open konst form: HTMLFormElement?
    open var align: String

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
 * Exposes the JavaScript [ValidityState](https://developer.mozilla.org/en/docs/Web/API/ValidityState) to Kotlin
 */
public external abstract class ValidityState : JsAny {
    open konst konstueMissing: Boolean
    open konst typeMismatch: Boolean
    open konst patternMismatch: Boolean
    open konst tooLong: Boolean
    open konst tooShort: Boolean
    open konst rangeUnderflow: Boolean
    open konst rangeOverflow: Boolean
    open konst stepMismatch: Boolean
    open konst badInput: Boolean
    open konst customError: Boolean
    open konst konstid: Boolean
}

/**
 * Exposes the JavaScript [HTMLDetailsElement](https://developer.mozilla.org/en/docs/Web/API/HTMLDetailsElement) to Kotlin
 */
public external abstract class HTMLDetailsElement : HTMLElement, JsAny {
    open var open: Boolean

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

public external abstract class HTMLMenuElement : HTMLElement, JsAny {
    open var type: String
    open var label: String
    open var compact: Boolean

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

public external abstract class HTMLMenuItemElement : HTMLElement, JsAny {
    open var type: String
    open var label: String
    open var icon: String
    open var disabled: Boolean
    open var checked: Boolean
    open var radiogroup: String
    open var default: Boolean

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

public external open class RelatedEvent(type: String, eventInitDict: RelatedEventInit = definedExternally) : Event, JsAny {
    open konst relatedTarget: EventTarget?

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface RelatedEventInit : EventInit, JsAny {
    var relatedTarget: EventTarget? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun RelatedEventInit(relatedTarget: EventTarget? = null, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): RelatedEventInit { js("return { relatedTarget, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [HTMLDialogElement](https://developer.mozilla.org/en/docs/Web/API/HTMLDialogElement) to Kotlin
 */
public external abstract class HTMLDialogElement : HTMLElement, JsAny {
    open var open: Boolean
    open var returnValue: String
    fun show(anchor: UnionElementOrMouseEvent = definedExternally)
    fun showModal(anchor: UnionElementOrMouseEvent = definedExternally)
    fun close(returnValue: String = definedExternally)

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
 * Exposes the JavaScript [HTMLScriptElement](https://developer.mozilla.org/en/docs/Web/API/HTMLScriptElement) to Kotlin
 */
public external abstract class HTMLScriptElement : HTMLElement, HTMLOrSVGScriptElement, JsAny {
    open var src: String
    open var type: String
    open var charset: String
    open var async: Boolean
    open var defer: Boolean
    open var crossOrigin: String?
    open var text: String
    open var nonce: String
    open var event: String
    open var htmlFor: String

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
 * Exposes the JavaScript [HTMLTemplateElement](https://developer.mozilla.org/en/docs/Web/API/HTMLTemplateElement) to Kotlin
 */
public external abstract class HTMLTemplateElement : HTMLElement, JsAny {
    open konst content: DocumentFragment

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
 * Exposes the JavaScript [HTMLSlotElement](https://developer.mozilla.org/en/docs/Web/API/HTMLSlotElement) to Kotlin
 */
public external abstract class HTMLSlotElement : HTMLElement, JsAny {
    open var name: String
    fun assignedNodes(options: AssignedNodesOptions = definedExternally): JsArray<Node>

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

public external interface AssignedNodesOptions : JsAny {
    var flatten: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun AssignedNodesOptions(flatten: Boolean? = false): AssignedNodesOptions { js("return { flatten };") }

/**
 * Exposes the JavaScript [HTMLCanvasElement](https://developer.mozilla.org/en/docs/Web/API/HTMLCanvasElement) to Kotlin
 */
public external abstract class HTMLCanvasElement : HTMLElement, CanvasImageSource, TexImageSource, JsAny {
    open var width: Int
    open var height: Int
    fun getContext(contextId: String, vararg arguments: JsAny?): RenderingContext?
    fun toDataURL(type: String = definedExternally, quality: JsAny? = definedExternally): String
    fun toBlob(_callback: (Blob?) -> Unit, type: String = definedExternally, quality: JsAny? = definedExternally)

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

public external interface CanvasRenderingContext2DSettings : JsAny {
    var alpha: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun CanvasRenderingContext2DSettings(alpha: Boolean? = true): CanvasRenderingContext2DSettings { js("return { alpha };") }

/**
 * Exposes the JavaScript [CanvasRenderingContext2D](https://developer.mozilla.org/en/docs/Web/API/CanvasRenderingContext2D) to Kotlin
 */
public external abstract class CanvasRenderingContext2D : CanvasState, CanvasTransform, CanvasCompositing, CanvasImageSmoothing, CanvasFillStrokeStyles, CanvasShadowStyles, CanvasFilters, CanvasRect, CanvasDrawPath, CanvasUserInterface, CanvasText, CanvasDrawImage, CanvasHitRegion, CanvasImageData, CanvasPathDrawingStyles, CanvasTextDrawingStyles, CanvasPath, RenderingContext, JsAny {
    open konst canvas: HTMLCanvasElement
}

public external interface CanvasState : JsAny {
    fun save()
    fun restore()
}

public external interface CanvasTransform : JsAny {
    fun scale(x: Double, y: Double)
    fun rotate(angle: Double)
    fun translate(x: Double, y: Double)
    fun transform(a: Double, b: Double, c: Double, d: Double, e: Double, f: Double)
    fun getTransform(): DOMMatrix
    fun setTransform(a: Double, b: Double, c: Double, d: Double, e: Double, f: Double)
    fun setTransform(transform: JsAny? = definedExternally)
    fun resetTransform()
}

public external interface CanvasCompositing : JsAny {
    var globalAlpha: Double
    var globalCompositeOperation: String
}

public external interface CanvasImageSmoothing : JsAny {
    var imageSmoothingEnabled: Boolean
    var imageSmoothingQuality: ImageSmoothingQuality
}

public external interface CanvasFillStrokeStyles : JsAny {
    var strokeStyle: JsAny?
    var fillStyle: JsAny?
    fun createLinearGradient(x0: Double, y0: Double, x1: Double, y1: Double): CanvasGradient
    fun createRadialGradient(x0: Double, y0: Double, r0: Double, x1: Double, y1: Double, r1: Double): CanvasGradient
    fun createPattern(image: CanvasImageSource, repetition: String): CanvasPattern?
}

public external interface CanvasShadowStyles : JsAny {
    var shadowOffsetX: Double
    var shadowOffsetY: Double
    var shadowBlur: Double
    var shadowColor: String
}

public external interface CanvasFilters : JsAny {
    var filter: String
}

public external interface CanvasRect : JsAny {
    fun clearRect(x: Double, y: Double, w: Double, h: Double)
    fun fillRect(x: Double, y: Double, w: Double, h: Double)
    fun strokeRect(x: Double, y: Double, w: Double, h: Double)
}

public external interface CanvasDrawPath : JsAny {
    fun beginPath()
    fun fill(fillRule: CanvasFillRule = definedExternally)
    fun fill(path: Path2D, fillRule: CanvasFillRule = definedExternally)
    fun stroke()
    fun stroke(path: Path2D)
    fun clip(fillRule: CanvasFillRule = definedExternally)
    fun clip(path: Path2D, fillRule: CanvasFillRule = definedExternally)
    fun resetClip()
    fun isPointInPath(x: Double, y: Double, fillRule: CanvasFillRule = definedExternally): Boolean
    fun isPointInPath(path: Path2D, x: Double, y: Double, fillRule: CanvasFillRule = definedExternally): Boolean
    fun isPointInStroke(x: Double, y: Double): Boolean
    fun isPointInStroke(path: Path2D, x: Double, y: Double): Boolean
}

public external interface CanvasUserInterface : JsAny {
    fun drawFocusIfNeeded(element: Element)
    fun drawFocusIfNeeded(path: Path2D, element: Element)
    fun scrollPathIntoView()
    fun scrollPathIntoView(path: Path2D)
}

public external interface CanvasText : JsAny {
    fun fillText(text: String, x: Double, y: Double, maxWidth: Double = definedExternally)
    fun strokeText(text: String, x: Double, y: Double, maxWidth: Double = definedExternally)
    fun measureText(text: String): TextMetrics
}

public external interface CanvasDrawImage : JsAny {
    fun drawImage(image: CanvasImageSource, dx: Double, dy: Double)
    fun drawImage(image: CanvasImageSource, dx: Double, dy: Double, dw: Double, dh: Double)
    fun drawImage(image: CanvasImageSource, sx: Double, sy: Double, sw: Double, sh: Double, dx: Double, dy: Double, dw: Double, dh: Double)
}

public external interface CanvasHitRegion : JsAny {
    fun addHitRegion(options: HitRegionOptions = definedExternally)
    fun removeHitRegion(id: String)
    fun clearHitRegions()
}

public external interface CanvasImageData : JsAny {
    fun createImageData(sw: Double, sh: Double): ImageData
    fun createImageData(imagedata: ImageData): ImageData
    fun getImageData(sx: Double, sy: Double, sw: Double, sh: Double): ImageData
    fun putImageData(imagedata: ImageData, dx: Double, dy: Double)
    fun putImageData(imagedata: ImageData, dx: Double, dy: Double, dirtyX: Double, dirtyY: Double, dirtyWidth: Double, dirtyHeight: Double)
}

public external interface CanvasPathDrawingStyles : JsAny {
    var lineWidth: Double
    var lineCap: CanvasLineCap
    var lineJoin: CanvasLineJoin
    var miterLimit: Double
    var lineDashOffset: Double
    fun setLineDash(segments: JsArray<JsNumber>)
    fun getLineDash(): JsArray<JsNumber>
}

public external interface CanvasTextDrawingStyles : JsAny {
    var font: String
    var textAlign: CanvasTextAlign
    var textBaseline: CanvasTextBaseline
    var direction: CanvasDirection
}

public external interface CanvasPath : JsAny {
    fun closePath()
    fun moveTo(x: Double, y: Double)
    fun lineTo(x: Double, y: Double)
    fun quadraticCurveTo(cpx: Double, cpy: Double, x: Double, y: Double)
    fun bezierCurveTo(cp1x: Double, cp1y: Double, cp2x: Double, cp2y: Double, x: Double, y: Double)
    fun arcTo(x1: Double, y1: Double, x2: Double, y2: Double, radius: Double)
    fun arcTo(x1: Double, y1: Double, x2: Double, y2: Double, radiusX: Double, radiusY: Double, rotation: Double)
    fun rect(x: Double, y: Double, w: Double, h: Double)
    fun arc(x: Double, y: Double, radius: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean = definedExternally)
    fun ellipse(x: Double, y: Double, radiusX: Double, radiusY: Double, rotation: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean = definedExternally)
}

/**
 * Exposes the JavaScript [CanvasGradient](https://developer.mozilla.org/en/docs/Web/API/CanvasGradient) to Kotlin
 */
public external abstract class CanvasGradient : JsAny {
    fun addColorStop(offset: Double, color: String)
}

/**
 * Exposes the JavaScript [CanvasPattern](https://developer.mozilla.org/en/docs/Web/API/CanvasPattern) to Kotlin
 */
public external abstract class CanvasPattern : JsAny {
    fun setTransform(transform: JsAny? = definedExternally)
}

/**
 * Exposes the JavaScript [TextMetrics](https://developer.mozilla.org/en/docs/Web/API/TextMetrics) to Kotlin
 */
public external abstract class TextMetrics : JsAny {
    open konst width: Double
    open konst actualBoundingBoxLeft: Double
    open konst actualBoundingBoxRight: Double
    open konst fontBoundingBoxAscent: Double
    open konst fontBoundingBoxDescent: Double
    open konst actualBoundingBoxAscent: Double
    open konst actualBoundingBoxDescent: Double
    open konst emHeightAscent: Double
    open konst emHeightDescent: Double
    open konst hangingBaseline: Double
    open konst alphabeticBaseline: Double
    open konst ideographicBaseline: Double
}

public external interface HitRegionOptions : JsAny {
    var path: Path2D? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var fillRule: CanvasFillRule? /* = CanvasFillRule.NONZERO */
        get() = definedExternally
        set(konstue) = definedExternally
    var id: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var parentID: String? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var cursor: String? /* = "inherit" */
        get() = definedExternally
        set(konstue) = definedExternally
    var control: Element? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var label: String? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var role: String? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun HitRegionOptions(path: Path2D? = null, fillRule: CanvasFillRule? = CanvasFillRule.NONZERO, id: String? = "", parentID: String? = null, cursor: String? = "inherit", control: Element? = null, label: String? = null, role: String? = null): HitRegionOptions { js("return { path, fillRule, id, parentID, cursor, control, label, role };") }

/**
 * Exposes the JavaScript [ImageData](https://developer.mozilla.org/en/docs/Web/API/ImageData) to Kotlin
 */
public external open class ImageData : ImageBitmapSource, TexImageSource, JsAny {
    constructor(sw: Int, sh: Int)
    constructor(data: Uint8ClampedArray, sw: Int, sh: Int = definedExternally)
    open konst width: Int
    open konst height: Int
    open konst data: Uint8ClampedArray
}

/**
 * Exposes the JavaScript [Path2D](https://developer.mozilla.org/en/docs/Web/API/Path2D) to Kotlin
 */
public external open class Path2D() : CanvasPath, JsAny {
    constructor(path: Path2D)
    constructor(paths: JsArray<Path2D>, fillRule: CanvasFillRule = definedExternally)
    constructor(d: String)
    fun addPath(path: Path2D, transform: JsAny? = definedExternally)
    override fun closePath()
    override fun moveTo(x: Double, y: Double)
    override fun lineTo(x: Double, y: Double)
    override fun quadraticCurveTo(cpx: Double, cpy: Double, x: Double, y: Double)
    override fun bezierCurveTo(cp1x: Double, cp1y: Double, cp2x: Double, cp2y: Double, x: Double, y: Double)
    override fun arcTo(x1: Double, y1: Double, x2: Double, y2: Double, radius: Double)
    override fun arcTo(x1: Double, y1: Double, x2: Double, y2: Double, radiusX: Double, radiusY: Double, rotation: Double)
    override fun rect(x: Double, y: Double, w: Double, h: Double)
    override fun arc(x: Double, y: Double, radius: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean /* = definedExternally */)
    override fun ellipse(x: Double, y: Double, radiusX: Double, radiusY: Double, rotation: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean /* = definedExternally */)
}

/**
 * Exposes the JavaScript [ImageBitmapRenderingContext](https://developer.mozilla.org/en/docs/Web/API/ImageBitmapRenderingContext) to Kotlin
 */
public external abstract class ImageBitmapRenderingContext : JsAny {
    open konst canvas: HTMLCanvasElement
    fun transferFromImageBitmap(bitmap: ImageBitmap?)
}

public external interface ImageBitmapRenderingContextSettings : JsAny {
    var alpha: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ImageBitmapRenderingContextSettings(alpha: Boolean? = true): ImageBitmapRenderingContextSettings { js("return { alpha };") }

/**
 * Exposes the JavaScript [CustomElementRegistry](https://developer.mozilla.org/en/docs/Web/API/CustomElementRegistry) to Kotlin
 */
public external abstract class CustomElementRegistry : JsAny {
    fun define(name: String, constructor: () -> JsAny?, options: ElementDefinitionOptions = definedExternally)
    fun get(name: String): JsAny?
    fun whenDefined(name: String): Promise<Nothing?>
}

public external interface ElementDefinitionOptions : JsAny {
    var extends: String?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ElementDefinitionOptions(extends: String? = undefined): ElementDefinitionOptions { js("return { extends };") }

public external interface ElementContentEditable : JsAny {
    var contentEditable: String
    konst isContentEditable: Boolean
}

/**
 * Exposes the JavaScript [DataTransfer](https://developer.mozilla.org/en/docs/Web/API/DataTransfer) to Kotlin
 */
public external abstract class DataTransfer : JsAny {
    open var dropEffect: String
    open var effectAllowed: String
    open konst items: DataTransferItemList
    open konst types: JsArray<out JsString>
    open konst files: FileList
    fun setDragImage(image: Element, x: Int, y: Int)
    fun getData(format: String): String
    fun setData(format: String, data: String)
    fun clearData(format: String = definedExternally)
}

/**
 * Exposes the JavaScript [DataTransferItemList](https://developer.mozilla.org/en/docs/Web/API/DataTransferItemList) to Kotlin
 */
public external abstract class DataTransferItemList : JsAny {
    open konst length: Int
    fun add(data: String, type: String): DataTransferItem?
    fun add(data: File): DataTransferItem?
    fun remove(index: Int)
    fun clear()
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForDataTransferItemList(obj: DataTransferItemList, index: Int): DataTransferItem? { js("return obj[index];") }

public operator fun DataTransferItemList.get(index: Int): DataTransferItem? = getMethodImplForDataTransferItemList(this, index)

/**
 * Exposes the JavaScript [DataTransferItem](https://developer.mozilla.org/en/docs/Web/API/DataTransferItem) to Kotlin
 */
public external abstract class DataTransferItem : JsAny {
    open konst kind: String
    open konst type: String
    fun getAsString(_callback: ((String) -> Unit)?)
    fun getAsFile(): File?
}

/**
 * Exposes the JavaScript [DragEvent](https://developer.mozilla.org/en/docs/Web/API/DragEvent) to Kotlin
 */
public external open class DragEvent(type: String, eventInitDict: DragEventInit = definedExternally) : MouseEvent, JsAny {
    open konst dataTransfer: DataTransfer?

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface DragEventInit : MouseEventInit, JsAny {
    var dataTransfer: DataTransfer? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun DragEventInit(dataTransfer: DataTransfer? = null, screenX: Int? = 0, screenY: Int? = 0, clientX: Int? = 0, clientY: Int? = 0, button: Short? = 0, buttons: Short? = 0, relatedTarget: EventTarget? = null, region: String? = null, ctrlKey: Boolean? = false, shiftKey: Boolean? = false, altKey: Boolean? = false, metaKey: Boolean? = false, modifierAltGraph: Boolean? = false, modifierCapsLock: Boolean? = false, modifierFn: Boolean? = false, modifierFnLock: Boolean? = false, modifierHyper: Boolean? = false, modifierNumLock: Boolean? = false, modifierScrollLock: Boolean? = false, modifierSuper: Boolean? = false, modifierSymbol: Boolean? = false, modifierSymbolLock: Boolean? = false, view: Window? = null, detail: Int? = 0, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): DragEventInit { js("return { dataTransfer, screenX, screenY, clientX, clientY, button, buttons, relatedTarget, region, ctrlKey, shiftKey, altKey, metaKey, modifierAltGraph, modifierCapsLock, modifierFn, modifierFnLock, modifierHyper, modifierNumLock, modifierScrollLock, modifierSuper, modifierSymbol, modifierSymbolLock, view, detail, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [Window](https://developer.mozilla.org/en/docs/Web/API/Window) to Kotlin
 */
public external abstract class Window : EventTarget, GlobalEventHandlers, WindowEventHandlers, WindowOrWorkerGlobalScope, WindowSessionStorage, WindowLocalStorage, GlobalPerformance, UnionMessagePortOrWindowProxy, JsAny {
    open konst window: Window
    open konst self: Window
    open konst document: Document
    open var name: String
    open konst location: Location
    open konst history: History
    open konst customElements: CustomElementRegistry
    open konst locationbar: BarProp
    open konst menubar: BarProp
    open konst personalbar: BarProp
    open konst scrollbars: BarProp
    open konst statusbar: BarProp
    open konst toolbar: BarProp
    open var status: String
    open konst closed: Boolean
    open konst frames: Window
    open konst length: Int
    open konst top: Window
    open var opener: JsAny?
    open konst parent: Window
    open konst frameElement: Element?
    open konst navigator: Navigator
    open konst applicationCache: ApplicationCache
    open konst external: External
    open konst screen: Screen
    open konst innerWidth: Int
    open konst innerHeight: Int
    open konst scrollX: Double
    open konst pageXOffset: Double
    open konst scrollY: Double
    open konst pageYOffset: Double
    open konst screenX: Int
    open konst screenY: Int
    open konst outerWidth: Int
    open konst outerHeight: Int
    open konst devicePixelRatio: Double
    fun close()
    fun stop()
    fun focus()
    fun blur()
    fun open(url: String = definedExternally, target: String = definedExternally, features: String = definedExternally): Window?
    fun alert()
    fun alert(message: String)
    fun confirm(message: String = definedExternally): Boolean
    fun prompt(message: String = definedExternally, default: String = definedExternally): String?
    fun print()
    fun requestAnimationFrame(callback: (Double) -> Unit): Int
    fun cancelAnimationFrame(handle: Int)
    fun postMessage(message: JsAny?, targetOrigin: String, transfer: JsArray<JsAny> = definedExternally)
    fun captureEvents()
    fun releaseEvents()
    fun matchMedia(query: String): MediaQueryList
    fun moveTo(x: Int, y: Int)
    fun moveBy(x: Int, y: Int)
    fun resizeTo(x: Int, y: Int)
    fun resizeBy(x: Int, y: Int)
    fun scroll(options: ScrollToOptions = definedExternally)
    fun scroll(x: Double, y: Double)
    fun scrollTo(options: ScrollToOptions = definedExternally)
    fun scrollTo(x: Double, y: Double)
    fun scrollBy(options: ScrollToOptions = definedExternally)
    fun scrollBy(x: Double, y: Double)
    fun getComputedStyle(elt: Element, pseudoElt: String? = definedExternally): CSSStyleDeclaration
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForWindow(obj: Window, name: String): JsAny? { js("return obj[name];") }

public operator fun Window.get(name: String): JsAny? = getMethodImplForWindow(this, name)

public external abstract class BarProp : JsAny {
    open konst visible: Boolean
}

/**
 * Exposes the JavaScript [History](https://developer.mozilla.org/en/docs/Web/API/History) to Kotlin
 */
public external abstract class History : JsAny {
    open konst length: Int
    open var scrollRestoration: ScrollRestoration
    open konst state: JsAny?
    fun go(delta: Int = definedExternally)
    fun back()
    fun forward()
    fun pushState(data: JsAny?, title: String, url: String? = definedExternally)
    fun replaceState(data: JsAny?, title: String, url: String? = definedExternally)
}

/**
 * Exposes the JavaScript [Location](https://developer.mozilla.org/en/docs/Web/API/Location) to Kotlin
 */
public external abstract class Location : JsAny {
    open var href: String
    open konst origin: String
    open var protocol: String
    open var host: String
    open var hostname: String
    open var port: String
    open var pathname: String
    open var search: String
    open var hash: String
    open konst ancestorOrigins: JsArray<out JsString>
    fun assign(url: String)
    fun replace(url: String)
    fun reload()
}

/**
 * Exposes the JavaScript [PopStateEvent](https://developer.mozilla.org/en/docs/Web/API/PopStateEvent) to Kotlin
 */
public external open class PopStateEvent(type: String, eventInitDict: PopStateEventInit = definedExternally) : Event, JsAny {
    open konst state: JsAny?

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface PopStateEventInit : EventInit, JsAny {
    var state: JsAny? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun PopStateEventInit(state: JsAny? = null, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): PopStateEventInit { js("return { state, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [HashChangeEvent](https://developer.mozilla.org/en/docs/Web/API/HashChangeEvent) to Kotlin
 */
public external open class HashChangeEvent(type: String, eventInitDict: HashChangeEventInit = definedExternally) : Event, JsAny {
    open konst oldURL: String
    open konst newURL: String

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface HashChangeEventInit : EventInit, JsAny {
    var oldURL: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var newURL: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun HashChangeEventInit(oldURL: String? = "", newURL: String? = "", bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): HashChangeEventInit { js("return { oldURL, newURL, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [PageTransitionEvent](https://developer.mozilla.org/en/docs/Web/API/PageTransitionEvent) to Kotlin
 */
public external open class PageTransitionEvent(type: String, eventInitDict: PageTransitionEventInit = definedExternally) : Event, JsAny {
    open konst persisted: Boolean

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface PageTransitionEventInit : EventInit, JsAny {
    var persisted: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun PageTransitionEventInit(persisted: Boolean? = false, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): PageTransitionEventInit { js("return { persisted, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [BeforeUnloadEvent](https://developer.mozilla.org/en/docs/Web/API/BeforeUnloadEvent) to Kotlin
 */
public external open class BeforeUnloadEvent : Event, JsAny {
    var returnValue: String

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external abstract class ApplicationCache : EventTarget, JsAny {
    open konst status: Short
    open var onchecking: ((Event) -> JsAny?)?
    open var onerror: ((Event) -> JsAny?)?
    open var onnoupdate: ((Event) -> JsAny?)?
    open var ondownloading: ((Event) -> JsAny?)?
    open var onprogress: ((ProgressEvent) -> JsAny?)?
    open var onupdateready: ((Event) -> JsAny?)?
    open var oncached: ((Event) -> JsAny?)?
    open var onobsolete: ((Event) -> JsAny?)?
    fun update()
    fun abort()
    fun swapCache()

    companion object {
        konst UNCACHED: Short
        konst IDLE: Short
        konst CHECKING: Short
        konst DOWNLOADING: Short
        konst UPDATEREADY: Short
        konst OBSOLETE: Short
    }
}

/**
 * Exposes the JavaScript [NavigatorOnLine](https://developer.mozilla.org/en/docs/Web/API/NavigatorOnLine) to Kotlin
 */
public external interface NavigatorOnLine : JsAny {
    konst onLine: Boolean
}

/**
 * Exposes the JavaScript [ErrorEvent](https://developer.mozilla.org/en/docs/Web/API/ErrorEvent) to Kotlin
 */
public external open class ErrorEvent(type: String, eventInitDict: ErrorEventInit = definedExternally) : Event, JsAny {
    open konst message: String
    open konst filename: String
    open konst lineno: Int
    open konst colno: Int
    open konst error: JsAny?

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface ErrorEventInit : EventInit, JsAny {
    var message: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var filename: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var lineno: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var colno: Int? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var error: JsAny? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ErrorEventInit(message: String? = "", filename: String? = "", lineno: Int? = 0, colno: Int? = 0, error: JsAny? = null, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): ErrorEventInit { js("return { message, filename, lineno, colno, error, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [PromiseRejectionEvent](https://developer.mozilla.org/en/docs/Web/API/PromiseRejectionEvent) to Kotlin
 */
public external open class PromiseRejectionEvent(type: String, eventInitDict: PromiseRejectionEventInit) : Event, JsAny {
    open konst promise: Promise<JsAny?>
    open konst reason: JsAny?

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface PromiseRejectionEventInit : EventInit, JsAny {
    var promise: Promise<JsAny?>?
    var reason: JsAny?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun PromiseRejectionEventInit(promise: Promise<JsAny?>?, reason: JsAny? = undefined, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): PromiseRejectionEventInit { js("return { promise, reason, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [GlobalEventHandlers](https://developer.mozilla.org/en/docs/Web/API/GlobalEventHandlers) to Kotlin
 */
public external interface GlobalEventHandlers : JsAny {
    var onabort: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onblur: ((FocusEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var oncancel: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var oncanplay: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var oncanplaythrough: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onchange: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onclick: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onclose: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var oncontextmenu: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var oncuechange: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondblclick: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondrag: ((DragEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondragend: ((DragEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondragenter: ((DragEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondragexit: ((DragEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondragleave: ((DragEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondragover: ((DragEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondragstart: ((DragEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondrop: ((DragEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ondurationchange: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onemptied: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onended: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onerror: ((JsAny?, String, Int, Int, JsAny?) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onfocus: ((FocusEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var oninput: ((InputEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var oninkonstid: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onkeydown: ((KeyboardEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onkeypress: ((KeyboardEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onkeyup: ((KeyboardEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onload: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onloadeddata: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onloadedmetadata: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onloadend: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onloadstart: ((ProgressEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onmousedown: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onmouseenter: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onmouseleave: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onmousemove: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onmouseout: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onmouseover: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onmouseup: ((MouseEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onwheel: ((WheelEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpause: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onplay: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onplaying: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onprogress: ((ProgressEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onratechange: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onreset: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onresize: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onscroll: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onseeked: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onseeking: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onselect: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onshow: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onstalled: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onsubmit: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onsuspend: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ontimeupdate: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ontoggle: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onvolumechange: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onwaiting: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ongotpointercapture: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onlostpointercapture: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpointerdown: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpointermove: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpointerup: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpointercancel: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpointerover: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpointerout: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpointerenter: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpointerleave: ((PointerEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
}

/**
 * Exposes the JavaScript [WindowEventHandlers](https://developer.mozilla.org/en/docs/Web/API/WindowEventHandlers) to Kotlin
 */
public external interface WindowEventHandlers : JsAny {
    var onafterprint: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onbeforeprint: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onbeforeunload: ((BeforeUnloadEvent) -> String?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onhashchange: ((HashChangeEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onlanguagechange: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onmessage: ((MessageEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onoffline: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var ononline: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpagehide: ((PageTransitionEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpageshow: ((PageTransitionEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpopstate: ((PopStateEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onrejectionhandled: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onstorage: ((StorageEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onunhandledrejection: ((PromiseRejectionEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onunload: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
}

public external interface DocumentAndElementEventHandlers : JsAny {
    var oncopy: ((ClipboardEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var oncut: ((ClipboardEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
    var onpaste: ((ClipboardEvent) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
}

/**
 * Exposes the JavaScript [WindowOrWorkerGlobalScope](https://developer.mozilla.org/en/docs/Web/API/WindowOrWorkerGlobalScope) to Kotlin
 */
public external interface WindowOrWorkerGlobalScope : JsAny {
    konst origin: String
    konst caches: CacheStorage
    fun btoa(data: String): String
    fun atob(data: String): String
    fun setTimeout(handler: JsAny?, timeout: Int = definedExternally, vararg arguments: JsAny?): Int
    fun clearTimeout(handle: Int = definedExternally)
    fun setInterkonst(handler: JsAny?, timeout: Int = definedExternally, vararg arguments: JsAny?): Int
    fun clearInterkonst(handle: Int = definedExternally)
    fun createImageBitmap(image: ImageBitmapSource, options: ImageBitmapOptions = definedExternally): Promise<ImageBitmap>
    fun createImageBitmap(image: ImageBitmapSource, sx: Int, sy: Int, sw: Int, sh: Int, options: ImageBitmapOptions = definedExternally): Promise<ImageBitmap>
    fun fetch(input: JsAny?, init: RequestInit = definedExternally): Promise<Response>
}

/**
 * Exposes the JavaScript [Navigator](https://developer.mozilla.org/en/docs/Web/API/Navigator) to Kotlin
 */
public external abstract class Navigator : NavigatorID, NavigatorLanguage, NavigatorOnLine, NavigatorContentUtils, NavigatorCookies, NavigatorPlugins, NavigatorConcurrentHardware, JsAny {
    open konst clipboard: Clipboard
    open konst mediaDevices: MediaDevices
    open konst maxTouchPoints: Int
    open konst serviceWorker: ServiceWorkerContainer
    fun requestMediaKeySystemAccess(keySystem: String, supportedConfigurations: JsArray<MediaKeySystemConfiguration>): Promise<MediaKeySystemAccess>
    fun getUserMedia(constraints: MediaStreamConstraints, successCallback: (MediaStream) -> Unit, errorCallback: (JsAny) -> Unit)
    fun vibrate(pattern: JsAny?): Boolean
}

/**
 * Exposes the JavaScript [NavigatorID](https://developer.mozilla.org/en/docs/Web/API/NavigatorID) to Kotlin
 */
public external interface NavigatorID : JsAny {
    konst appCodeName: String
    konst appName: String
    konst appVersion: String
    konst platform: String
    konst product: String
    konst productSub: String
    konst userAgent: String
    konst vendor: String
    konst vendorSub: String
    konst oscpu: String
    fun taintEnabled(): Boolean
}

/**
 * Exposes the JavaScript [NavigatorLanguage](https://developer.mozilla.org/en/docs/Web/API/NavigatorLanguage) to Kotlin
 */
public external interface NavigatorLanguage : JsAny {
    konst language: String
    konst languages: JsArray<out JsString>
}

public external interface NavigatorContentUtils : JsAny {
    fun registerProtocolHandler(scheme: String, url: String, title: String)
    fun registerContentHandler(mimeType: String, url: String, title: String)
    fun isProtocolHandlerRegistered(scheme: String, url: String): String
    fun isContentHandlerRegistered(mimeType: String, url: String): String
    fun unregisterProtocolHandler(scheme: String, url: String)
    fun unregisterContentHandler(mimeType: String, url: String)
}

public external interface NavigatorCookies : JsAny {
    konst cookieEnabled: Boolean
}

/**
 * Exposes the JavaScript [NavigatorPlugins](https://developer.mozilla.org/en/docs/Web/API/NavigatorPlugins) to Kotlin
 */
public external interface NavigatorPlugins : JsAny {
    konst plugins: PluginArray
    konst mimeTypes: MimeTypeArray
    fun javaEnabled(): Boolean
}

/**
 * Exposes the JavaScript [PluginArray](https://developer.mozilla.org/en/docs/Web/API/PluginArray) to Kotlin
 */
public external abstract class PluginArray : ItemArrayLike<Plugin>, JsAny {
    fun refresh(reload: Boolean = definedExternally)
    override fun item(index: Int): Plugin?
    fun namedItem(name: String): Plugin?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForPluginArray(obj: PluginArray, index: Int): Plugin? { js("return obj[index];") }

public operator fun PluginArray.get(index: Int): Plugin? = getMethodImplForPluginArray(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForPluginArray(obj: PluginArray, name: String): Plugin? { js("return obj[name];") }

public operator fun PluginArray.get(name: String): Plugin? = getMethodImplForPluginArray(this, name)

/**
 * Exposes the JavaScript [MimeTypeArray](https://developer.mozilla.org/en/docs/Web/API/MimeTypeArray) to Kotlin
 */
public external abstract class MimeTypeArray : ItemArrayLike<MimeType>, JsAny {
    override fun item(index: Int): MimeType?
    fun namedItem(name: String): MimeType?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForMimeTypeArray(obj: MimeTypeArray, index: Int): MimeType? { js("return obj[index];") }

public operator fun MimeTypeArray.get(index: Int): MimeType? = getMethodImplForMimeTypeArray(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForMimeTypeArray(obj: MimeTypeArray, name: String): MimeType? { js("return obj[name];") }

public operator fun MimeTypeArray.get(name: String): MimeType? = getMethodImplForMimeTypeArray(this, name)

/**
 * Exposes the JavaScript [Plugin](https://developer.mozilla.org/en/docs/Web/API/Plugin) to Kotlin
 */
public external abstract class Plugin : ItemArrayLike<MimeType>, JsAny {
    open konst name: String
    open konst description: String
    open konst filename: String
    override fun item(index: Int): MimeType?
    fun namedItem(name: String): MimeType?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForPlugin(obj: Plugin, index: Int): MimeType? { js("return obj[index];") }

public operator fun Plugin.get(index: Int): MimeType? = getMethodImplForPlugin(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForPlugin(obj: Plugin, name: String): MimeType? { js("return obj[name];") }

public operator fun Plugin.get(name: String): MimeType? = getMethodImplForPlugin(this, name)

/**
 * Exposes the JavaScript [MimeType](https://developer.mozilla.org/en/docs/Web/API/MimeType) to Kotlin
 */
public external abstract class MimeType : JsAny {
    open konst type: String
    open konst description: String
    open konst suffixes: String
    open konst enabledPlugin: Plugin
}

/**
 * Exposes the JavaScript [ImageBitmap](https://developer.mozilla.org/en/docs/Web/API/ImageBitmap) to Kotlin
 */
public external abstract class ImageBitmap : CanvasImageSource, TexImageSource, JsAny {
    open konst width: Int
    open konst height: Int
    fun close()
}

public external interface ImageBitmapOptions : JsAny {
    var imageOrientation: ImageOrientation? /* = ImageOrientation.NONE */
        get() = definedExternally
        set(konstue) = definedExternally
    var premultiplyAlpha: PremultiplyAlpha? /* = PremultiplyAlpha.DEFAULT */
        get() = definedExternally
        set(konstue) = definedExternally
    var colorSpaceConversion: ColorSpaceConversion? /* = ColorSpaceConversion.DEFAULT */
        get() = definedExternally
        set(konstue) = definedExternally
    var resizeWidth: Int?
        get() = definedExternally
        set(konstue) = definedExternally
    var resizeHeight: Int?
        get() = definedExternally
        set(konstue) = definedExternally
    var resizeQuality: ResizeQuality? /* = ResizeQuality.LOW */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ImageBitmapOptions(imageOrientation: ImageOrientation? = ImageOrientation.NONE, premultiplyAlpha: PremultiplyAlpha? = PremultiplyAlpha.DEFAULT, colorSpaceConversion: ColorSpaceConversion? = ColorSpaceConversion.DEFAULT, resizeWidth: Int? = undefined, resizeHeight: Int? = undefined, resizeQuality: ResizeQuality? = ResizeQuality.LOW): ImageBitmapOptions { js("return { imageOrientation, premultiplyAlpha, colorSpaceConversion, resizeWidth, resizeHeight, resizeQuality };") }

/**
 * Exposes the JavaScript [MessageEvent](https://developer.mozilla.org/en/docs/Web/API/MessageEvent) to Kotlin
 */
public external open class MessageEvent(type: String, eventInitDict: MessageEventInit = definedExternally) : Event, JsAny {
    open konst data: JsAny?
    open konst origin: String
    open konst lastEventId: String
    open konst source: UnionMessagePortOrWindowProxy?
    open konst ports: JsArray<out MessagePort>
    fun initMessageEvent(type: String, bubbles: Boolean, cancelable: Boolean, data: JsAny?, origin: String, lastEventId: String, source: UnionMessagePortOrWindowProxy?, ports: JsArray<MessagePort>)

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface MessageEventInit : EventInit, JsAny {
    var data: JsAny? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var origin: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var lastEventId: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var source: UnionMessagePortOrWindowProxy? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var ports: JsArray<MessagePort>? /* = arrayOf() */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun MessageEventInit(data: JsAny? = null, origin: String? = "", lastEventId: String? = "", source: UnionMessagePortOrWindowProxy? = null, ports: JsArray<MessagePort>? = JsArray(), bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): MessageEventInit { js("return { data, origin, lastEventId, source, ports, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [EventSource](https://developer.mozilla.org/en/docs/Web/API/EventSource) to Kotlin
 */
public external open class EventSource(url: String, eventSourceInitDict: EventSourceInit = definedExternally) : EventTarget, JsAny {
    open konst url: String
    open konst withCredentials: Boolean
    open konst readyState: Short
    var onopen: ((Event) -> JsAny?)?
    var onmessage: ((MessageEvent) -> JsAny?)?
    var onerror: ((Event) -> JsAny?)?
    fun close()

    companion object {
        konst CONNECTING: Short
        konst OPEN: Short
        konst CLOSED: Short
    }
}

public external interface EventSourceInit : JsAny {
    var withCredentials: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun EventSourceInit(withCredentials: Boolean? = false): EventSourceInit { js("return { withCredentials };") }

/**
 * Exposes the JavaScript [WebSocket](https://developer.mozilla.org/en/docs/Web/API/WebSocket) to Kotlin
 */
public external open class WebSocket(url: String, protocols: JsAny? = definedExternally) : EventTarget, JsAny {
    open konst url: String
    open konst readyState: Short
    open konst bufferedAmount: JsNumber
    var onopen: ((Event) -> JsAny?)?
    var onerror: ((Event) -> JsAny?)?
    var onclose: ((Event) -> JsAny?)?
    open konst extensions: String
    open konst protocol: String
    var onmessage: ((MessageEvent) -> JsAny?)?
    var binaryType: BinaryType
    fun close(code: Short = definedExternally, reason: String = definedExternally)
    fun send(data: String)
    fun send(data: Blob)
    fun send(data: ArrayBuffer)
    fun send(data: ArrayBufferView)

    companion object {
        konst CONNECTING: Short
        konst OPEN: Short
        konst CLOSING: Short
        konst CLOSED: Short
    }
}

/**
 * Exposes the JavaScript [CloseEvent](https://developer.mozilla.org/en/docs/Web/API/CloseEvent) to Kotlin
 */
public external open class CloseEvent(type: String, eventInitDict: CloseEventInit = definedExternally) : Event, JsAny {
    open konst wasClean: Boolean
    open konst code: Short
    open konst reason: String

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface CloseEventInit : EventInit, JsAny {
    var wasClean: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var code: Short? /* = 0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var reason: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun CloseEventInit(wasClean: Boolean? = false, code: Short? = 0, reason: String? = "", bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): CloseEventInit { js("return { wasClean, code, reason, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [MessageChannel](https://developer.mozilla.org/en/docs/Web/API/MessageChannel) to Kotlin
 */
public external open class MessageChannel : JsAny {
    open konst port1: MessagePort
    open konst port2: MessagePort
}

/**
 * Exposes the JavaScript [MessagePort](https://developer.mozilla.org/en/docs/Web/API/MessagePort) to Kotlin
 */
public external abstract class MessagePort : EventTarget, UnionMessagePortOrWindowProxy, UnionMessagePortOrServiceWorker, UnionClientOrMessagePortOrServiceWorker, JsAny {
    open var onmessage: ((MessageEvent) -> JsAny?)?
    fun postMessage(message: JsAny?, transfer: JsArray<JsAny> = definedExternally)
    fun start()
    fun close()
}

/**
 * Exposes the JavaScript [BroadcastChannel](https://developer.mozilla.org/en/docs/Web/API/BroadcastChannel) to Kotlin
 */
public external open class BroadcastChannel(name: String) : EventTarget, JsAny {
    open konst name: String
    var onmessage: ((MessageEvent) -> JsAny?)?
    fun postMessage(message: JsAny?)
    fun close()
}

/**
 * Exposes the JavaScript [WorkerGlobalScope](https://developer.mozilla.org/en/docs/Web/API/WorkerGlobalScope) to Kotlin
 */
public external abstract class WorkerGlobalScope : EventTarget, WindowOrWorkerGlobalScope, GlobalPerformance, JsAny {
    open konst self: WorkerGlobalScope
    open konst location: WorkerLocation
    open konst navigator: WorkerNavigator
    open var onerror: ((JsAny?, String, Int, Int, JsAny?) -> JsAny?)?
    open var onlanguagechange: ((Event) -> JsAny?)?
    open var onoffline: ((Event) -> JsAny?)?
    open var ononline: ((Event) -> JsAny?)?
    open var onrejectionhandled: ((Event) -> JsAny?)?
    open var onunhandledrejection: ((PromiseRejectionEvent) -> JsAny?)?
    fun importScripts(vararg urls: String)
}

/**
 * Exposes the JavaScript [DedicatedWorkerGlobalScope](https://developer.mozilla.org/en/docs/Web/API/DedicatedWorkerGlobalScope) to Kotlin
 */
public external abstract class DedicatedWorkerGlobalScope : WorkerGlobalScope, JsAny {
    open var onmessage: ((MessageEvent) -> JsAny?)?
    fun postMessage(message: JsAny?, transfer: JsArray<JsAny> = definedExternally)
    fun close()
}

/**
 * Exposes the JavaScript [SharedWorkerGlobalScope](https://developer.mozilla.org/en/docs/Web/API/SharedWorkerGlobalScope) to Kotlin
 */
public external abstract class SharedWorkerGlobalScope : WorkerGlobalScope, JsAny {
    open konst name: String
    open konst applicationCache: ApplicationCache
    open var onconnect: ((Event) -> JsAny?)?
    fun close()
}

/**
 * Exposes the JavaScript [AbstractWorker](https://developer.mozilla.org/en/docs/Web/API/AbstractWorker) to Kotlin
 */
public external interface AbstractWorker : JsAny {
    var onerror: ((Event) -> JsAny?)?
        get() = definedExternally
        set(konstue) = definedExternally
}

/**
 * Exposes the JavaScript [Worker](https://developer.mozilla.org/en/docs/Web/API/Worker) to Kotlin
 */
public external open class Worker(scriptURL: String, options: WorkerOptions = definedExternally) : EventTarget, AbstractWorker, JsAny {
    var onmessage: ((MessageEvent) -> JsAny?)?
    override var onerror: ((Event) -> JsAny?)?
    fun terminate()
    fun postMessage(message: JsAny?, transfer: JsArray<JsAny> = definedExternally)
}

public external interface WorkerOptions : JsAny {
    var type: WorkerType? /* = WorkerType.CLASSIC */
        get() = definedExternally
        set(konstue) = definedExternally
    var credentials: RequestCredentials? /* = RequestCredentials.OMIT */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun WorkerOptions(type: WorkerType? = WorkerType.CLASSIC, credentials: RequestCredentials? = RequestCredentials.OMIT): WorkerOptions { js("return { type, credentials };") }

/**
 * Exposes the JavaScript [SharedWorker](https://developer.mozilla.org/en/docs/Web/API/SharedWorker) to Kotlin
 */
public external open class SharedWorker(scriptURL: String, name: String = definedExternally, options: WorkerOptions = definedExternally) : EventTarget, AbstractWorker, JsAny {
    open konst port: MessagePort
    override var onerror: ((Event) -> JsAny?)?
}

/**
 * Exposes the JavaScript [NavigatorConcurrentHardware](https://developer.mozilla.org/en/docs/Web/API/NavigatorConcurrentHardware) to Kotlin
 */
public external interface NavigatorConcurrentHardware : JsAny {
    konst hardwareConcurrency: JsNumber
}

/**
 * Exposes the JavaScript [WorkerNavigator](https://developer.mozilla.org/en/docs/Web/API/WorkerNavigator) to Kotlin
 */
public external abstract class WorkerNavigator : NavigatorID, NavigatorLanguage, NavigatorOnLine, NavigatorConcurrentHardware, JsAny {
    open konst serviceWorker: ServiceWorkerContainer
}

/**
 * Exposes the JavaScript [WorkerLocation](https://developer.mozilla.org/en/docs/Web/API/WorkerLocation) to Kotlin
 */
public external abstract class WorkerLocation : JsAny {
    open konst href: String
    open konst origin: String
    open konst protocol: String
    open konst host: String
    open konst hostname: String
    open konst port: String
    open konst pathname: String
    open konst search: String
    open konst hash: String
}

/**
 * Exposes the JavaScript [Storage](https://developer.mozilla.org/en/docs/Web/API/Storage) to Kotlin
 */
public external abstract class Storage : JsAny {
    open konst length: Int
    fun key(index: Int): String?
    fun removeItem(key: String)
    fun clear()
    fun getItem(key: String): String?
    fun setItem(key: String, konstue: String)
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForStorage(obj: Storage, key: String): String? { js("return obj[key];") }

public operator fun Storage.get(key: String): String? = getMethodImplForStorage(this, key)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForStorage(obj: Storage, key: String, konstue: String) { js("obj[key] = konstue;") }

public operator fun Storage.set(key: String, konstue: String) = setMethodImplForStorage(this, key, konstue)

/**
 * Exposes the JavaScript [WindowSessionStorage](https://developer.mozilla.org/en/docs/Web/API/WindowSessionStorage) to Kotlin
 */
public external interface WindowSessionStorage : JsAny {
    konst sessionStorage: Storage
}

/**
 * Exposes the JavaScript [WindowLocalStorage](https://developer.mozilla.org/en/docs/Web/API/WindowLocalStorage) to Kotlin
 */
public external interface WindowLocalStorage : JsAny {
    konst localStorage: Storage
}

/**
 * Exposes the JavaScript [StorageEvent](https://developer.mozilla.org/en/docs/Web/API/StorageEvent) to Kotlin
 */
public external open class StorageEvent(type: String, eventInitDict: StorageEventInit = definedExternally) : Event, JsAny {
    open konst key: String?
    open konst oldValue: String?
    open konst newValue: String?
    open konst url: String
    open konst storageArea: Storage?

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface StorageEventInit : EventInit, JsAny {
    var key: String? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var oldValue: String? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var newValue: String? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
    var url: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var storageArea: Storage? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun StorageEventInit(key: String? = null, oldValue: String? = null, newValue: String? = null, url: String? = "", storageArea: Storage? = null, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): StorageEventInit { js("return { key, oldValue, newValue, url, storageArea, bubbles, cancelable, composed };") }

public external abstract class HTMLAppletElement : HTMLElement, JsAny {
    open var align: String
    open var alt: String
    open var archive: String
    open var code: String
    open var codeBase: String
    open var height: String
    open var hspace: Int
    open var name: String
    open var _object: String
    open var vspace: Int
    open var width: String

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
 * Exposes the JavaScript [HTMLMarqueeElement](https://developer.mozilla.org/en/docs/Web/API/HTMLMarqueeElement) to Kotlin
 */
public external abstract class HTMLMarqueeElement : HTMLElement, JsAny {
    open var behavior: String
    open var bgColor: String
    open var direction: String
    open var height: String
    open var hspace: Int
    open var loop: Int
    open var scrollAmount: Int
    open var scrollDelay: Int
    open var trueSpeed: Boolean
    open var vspace: Int
    open var width: String
    open var onbounce: ((Event) -> JsAny?)?
    open var onfinish: ((Event) -> JsAny?)?
    open var onstart: ((Event) -> JsAny?)?
    fun start()
    fun stop()

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
 * Exposes the JavaScript [HTMLFrameSetElement](https://developer.mozilla.org/en/docs/Web/API/HTMLFrameSetElement) to Kotlin
 */
public external abstract class HTMLFrameSetElement : HTMLElement, WindowEventHandlers, JsAny {
    open var cols: String
    open var rows: String

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

public external abstract class HTMLFrameElement : HTMLElement, JsAny {
    open var name: String
    open var scrolling: String
    open var src: String
    open var frameBorder: String
    open var longDesc: String
    open var noResize: Boolean
    open konst contentDocument: Document?
    open konst contentWindow: Window?
    open var marginHeight: String
    open var marginWidth: String

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

public external abstract class HTMLDirectoryElement : HTMLElement, JsAny {
    open var compact: Boolean

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
 * Exposes the JavaScript [HTMLFontElement](https://developer.mozilla.org/en/docs/Web/API/HTMLFontElement) to Kotlin
 */
public external abstract class HTMLFontElement : HTMLElement, JsAny {
    open var color: String
    open var face: String
    open var size: String

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

public external interface External : JsAny {
    fun AddSearchProvider()
    fun IsSearchProviderInstalled()
}

public external interface EventInit : JsAny {
    var bubbles: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var cancelable: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var composed: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun EventInit(bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): EventInit { js("return { bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [CustomEvent](https://developer.mozilla.org/en/docs/Web/API/CustomEvent) to Kotlin
 */
public external open class CustomEvent(type: String, eventInitDict: CustomEventInit = definedExternally) : Event, JsAny {
    open konst detail: JsAny?
    fun initCustomEvent(type: String, bubbles: Boolean, cancelable: Boolean, detail: JsAny?)

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface CustomEventInit : EventInit, JsAny {
    var detail: JsAny? /* = null */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun CustomEventInit(detail: JsAny? = null, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): CustomEventInit { js("return { detail, bubbles, cancelable, composed };") }

public external interface EventListenerOptions : JsAny {
    var capture: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun EventListenerOptions(capture: Boolean? = false): EventListenerOptions { js("return { capture };") }

public external interface AddEventListenerOptions : EventListenerOptions, JsAny {
    var passive: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var once: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun AddEventListenerOptions(passive: Boolean? = false, once: Boolean? = false, capture: Boolean? = false): AddEventListenerOptions { js("return { passive, once, capture };") }

public external interface NonElementParentNode : JsAny {
    fun getElementById(elementId: String): Element?
}

/**
 * Exposes the JavaScript [DocumentOrShadowRoot](https://developer.mozilla.org/en/docs/Web/API/DocumentOrShadowRoot) to Kotlin
 */
public external interface DocumentOrShadowRoot : JsAny {
    konst fullscreenElement: Element?
        get() = definedExternally
}

/**
 * Exposes the JavaScript [ParentNode](https://developer.mozilla.org/en/docs/Web/API/ParentNode) to Kotlin
 */
public external interface ParentNode : JsAny {
    konst children: HTMLCollection
    konst firstElementChild: Element?
        get() = definedExternally
    konst lastElementChild: Element?
        get() = definedExternally
    konst childElementCount: Int
    fun prepend(vararg nodes: JsAny?)
    fun append(vararg nodes: JsAny?)
    fun querySelector(selectors: String): Element?
    fun querySelectorAll(selectors: String): NodeList
}

/**
 * Exposes the JavaScript [NonDocumentTypeChildNode](https://developer.mozilla.org/en/docs/Web/API/NonDocumentTypeChildNode) to Kotlin
 */
public external interface NonDocumentTypeChildNode : JsAny {
    konst previousElementSibling: Element?
        get() = definedExternally
    konst nextElementSibling: Element?
        get() = definedExternally
}

/**
 * Exposes the JavaScript [ChildNode](https://developer.mozilla.org/en/docs/Web/API/ChildNode) to Kotlin
 */
public external interface ChildNode : JsAny {
    fun before(vararg nodes: JsAny?)
    fun after(vararg nodes: JsAny?)
    fun replaceWith(vararg nodes: JsAny?)
    fun remove()
}

/**
 * Exposes the JavaScript [Slotable](https://developer.mozilla.org/en/docs/Web/API/Slotable) to Kotlin
 */
public external interface Slotable : JsAny {
    konst assignedSlot: HTMLSlotElement?
        get() = definedExternally
}

/**
 * Exposes the JavaScript [NodeList](https://developer.mozilla.org/en/docs/Web/API/NodeList) to Kotlin
 */
public external abstract class NodeList : ItemArrayLike<Node>, JsAny {
    override fun item(index: Int): Node?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForNodeList(obj: NodeList, index: Int): Node? { js("return obj[index];") }

public operator fun NodeList.get(index: Int): Node? = getMethodImplForNodeList(this, index)

/**
 * Exposes the JavaScript [HTMLCollection](https://developer.mozilla.org/en/docs/Web/API/HTMLCollection) to Kotlin
 */
public external abstract class HTMLCollection : ItemArrayLike<Element>, UnionElementOrHTMLCollection, JsAny {
    override fun item(index: Int): Element?
    fun namedItem(name: String): Element?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForHTMLCollection(obj: HTMLCollection, index: Int): Element? { js("return obj[index];") }

public operator fun HTMLCollection.get(index: Int): Element? = getMethodImplForHTMLCollection(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForHTMLCollection(obj: HTMLCollection, name: String): Element? { js("return obj[name];") }

public operator fun HTMLCollection.get(name: String): Element? = getMethodImplForHTMLCollection(this, name)

/**
 * Exposes the JavaScript [MutationObserver](https://developer.mozilla.org/en/docs/Web/API/MutationObserver) to Kotlin
 */
public external open class MutationObserver(callback: (JsArray<MutationRecord>, MutationObserver) -> Unit) : JsAny {
    fun observe(target: Node, options: MutationObserverInit = definedExternally)
    fun disconnect()
    fun takeRecords(): JsArray<MutationRecord>
}

/**
 * Exposes the JavaScript [MutationObserverInit](https://developer.mozilla.org/en/docs/Web/API/MutationObserverInit) to Kotlin
 */
public external interface MutationObserverInit : JsAny {
    var childList: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var attributes: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
    var characterData: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
    var subtree: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var attributeOldValue: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
    var characterDataOldValue: Boolean?
        get() = definedExternally
        set(konstue) = definedExternally
    var attributeFilter: JsArray<JsString>?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun MutationObserverInit(childList: Boolean? = false, attributes: Boolean? = undefined, characterData: Boolean? = undefined, subtree: Boolean? = false, attributeOldValue: Boolean? = undefined, characterDataOldValue: Boolean? = undefined, attributeFilter: JsArray<JsString>? = undefined): MutationObserverInit { js("return { childList, attributes, characterData, subtree, attributeOldValue, characterDataOldValue, attributeFilter };") }

/**
 * Exposes the JavaScript [MutationRecord](https://developer.mozilla.org/en/docs/Web/API/MutationRecord) to Kotlin
 */
public external abstract class MutationRecord : JsAny {
    open konst type: String
    open konst target: Node
    open konst addedNodes: NodeList
    open konst removedNodes: NodeList
    open konst previousSibling: Node?
    open konst nextSibling: Node?
    open konst attributeName: String?
    open konst attributeNamespace: String?
    open konst oldValue: String?
}

/**
 * Exposes the JavaScript [Node](https://developer.mozilla.org/en/docs/Web/API/Node) to Kotlin
 */
public external abstract class Node : EventTarget, JsAny {
    open konst nodeType: Short
    open konst nodeName: String
    open konst baseURI: String
    open konst isConnected: Boolean
    open konst ownerDocument: Document?
    open konst parentNode: Node?
    open konst parentElement: Element?
    open konst childNodes: NodeList
    open konst firstChild: Node?
    open konst lastChild: Node?
    open konst previousSibling: Node?
    open konst nextSibling: Node?
    open var nodeValue: String?
    open var textContent: String?
    fun getRootNode(options: GetRootNodeOptions = definedExternally): Node
    fun hasChildNodes(): Boolean
    fun normalize()
    fun cloneNode(deep: Boolean = definedExternally): Node
    fun isEqualNode(otherNode: Node?): Boolean
    fun isSameNode(otherNode: Node?): Boolean
    fun compareDocumentPosition(other: Node): Short
    fun contains(other: Node?): Boolean
    fun lookupPrefix(namespace: String?): String?
    fun lookupNamespaceURI(prefix: String?): String?
    fun isDefaultNamespace(namespace: String?): Boolean
    fun insertBefore(node: Node, child: Node?): Node
    fun appendChild(node: Node): Node
    fun replaceChild(node: Node, child: Node): Node
    fun removeChild(child: Node): Node

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

public external interface GetRootNodeOptions : JsAny {
    var composed: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun GetRootNodeOptions(composed: Boolean? = false): GetRootNodeOptions { js("return { composed };") }

/**
 * Exposes the JavaScript [Document](https://developer.mozilla.org/en/docs/Web/API/Document) to Kotlin
 */
public external open class Document : Node, GlobalEventHandlers, DocumentAndElementEventHandlers, NonElementParentNode, DocumentOrShadowRoot, ParentNode, GeometryUtils, JsAny {
    open konst implementation: DOMImplementation
    open konst URL: String
    open konst documentURI: String
    open konst origin: String
    open konst compatMode: String
    open konst characterSet: String
    open konst charset: String
    open konst inputEncoding: String
    open konst contentType: String
    open konst doctype: DocumentType?
    open konst documentElement: Element?
    open konst location: Location?
    var domain: String
    open konst referrer: String
    var cookie: String
    open konst lastModified: String
    open konst readyState: DocumentReadyState
    var title: String
    var dir: String
    var body: HTMLElement?
    open konst head: HTMLHeadElement?
    open konst images: HTMLCollection
    open konst embeds: HTMLCollection
    open konst plugins: HTMLCollection
    open konst links: HTMLCollection
    open konst forms: HTMLCollection
    open konst scripts: HTMLCollection
    open konst currentScript: HTMLOrSVGScriptElement?
    open konst defaultView: Window?
    open konst activeElement: Element?
    var designMode: String
    var onreadystatechange: ((Event) -> JsAny?)?
    var fgColor: String
    var linkColor: String
    var vlinkColor: String
    var alinkColor: String
    var bgColor: String
    open konst anchors: HTMLCollection
    open konst applets: HTMLCollection
    open konst all: HTMLAllCollection
    open konst scrollingElement: Element?
    open konst styleSheets: StyleSheetList
    open konst rootElement: SVGSVGElement?
    open konst fullscreenEnabled: Boolean
    open konst fullscreen: Boolean
    var onfullscreenchange: ((Event) -> JsAny?)?
    var onfullscreenerror: ((Event) -> JsAny?)?
    override var onabort: ((Event) -> JsAny?)?
    override var onblur: ((FocusEvent) -> JsAny?)?
    override var oncancel: ((Event) -> JsAny?)?
    override var oncanplay: ((Event) -> JsAny?)?
    override var oncanplaythrough: ((Event) -> JsAny?)?
    override var onchange: ((Event) -> JsAny?)?
    override var onclick: ((MouseEvent) -> JsAny?)?
    override var onclose: ((Event) -> JsAny?)?
    override var oncontextmenu: ((MouseEvent) -> JsAny?)?
    override var oncuechange: ((Event) -> JsAny?)?
    override var ondblclick: ((MouseEvent) -> JsAny?)?
    override var ondrag: ((DragEvent) -> JsAny?)?
    override var ondragend: ((DragEvent) -> JsAny?)?
    override var ondragenter: ((DragEvent) -> JsAny?)?
    override var ondragexit: ((DragEvent) -> JsAny?)?
    override var ondragleave: ((DragEvent) -> JsAny?)?
    override var ondragover: ((DragEvent) -> JsAny?)?
    override var ondragstart: ((DragEvent) -> JsAny?)?
    override var ondrop: ((DragEvent) -> JsAny?)?
    override var ondurationchange: ((Event) -> JsAny?)?
    override var onemptied: ((Event) -> JsAny?)?
    override var onended: ((Event) -> JsAny?)?
    override var onerror: ((JsAny?, String, Int, Int, JsAny?) -> JsAny?)?
    override var onfocus: ((FocusEvent) -> JsAny?)?
    override var oninput: ((InputEvent) -> JsAny?)?
    override var oninkonstid: ((Event) -> JsAny?)?
    override var onkeydown: ((KeyboardEvent) -> JsAny?)?
    override var onkeypress: ((KeyboardEvent) -> JsAny?)?
    override var onkeyup: ((KeyboardEvent) -> JsAny?)?
    override var onload: ((Event) -> JsAny?)?
    override var onloadeddata: ((Event) -> JsAny?)?
    override var onloadedmetadata: ((Event) -> JsAny?)?
    override var onloadend: ((Event) -> JsAny?)?
    override var onloadstart: ((ProgressEvent) -> JsAny?)?
    override var onmousedown: ((MouseEvent) -> JsAny?)?
    override var onmouseenter: ((MouseEvent) -> JsAny?)?
    override var onmouseleave: ((MouseEvent) -> JsAny?)?
    override var onmousemove: ((MouseEvent) -> JsAny?)?
    override var onmouseout: ((MouseEvent) -> JsAny?)?
    override var onmouseover: ((MouseEvent) -> JsAny?)?
    override var onmouseup: ((MouseEvent) -> JsAny?)?
    override var onwheel: ((WheelEvent) -> JsAny?)?
    override var onpause: ((Event) -> JsAny?)?
    override var onplay: ((Event) -> JsAny?)?
    override var onplaying: ((Event) -> JsAny?)?
    override var onprogress: ((ProgressEvent) -> JsAny?)?
    override var onratechange: ((Event) -> JsAny?)?
    override var onreset: ((Event) -> JsAny?)?
    override var onresize: ((Event) -> JsAny?)?
    override var onscroll: ((Event) -> JsAny?)?
    override var onseeked: ((Event) -> JsAny?)?
    override var onseeking: ((Event) -> JsAny?)?
    override var onselect: ((Event) -> JsAny?)?
    override var onshow: ((Event) -> JsAny?)?
    override var onstalled: ((Event) -> JsAny?)?
    override var onsubmit: ((Event) -> JsAny?)?
    override var onsuspend: ((Event) -> JsAny?)?
    override var ontimeupdate: ((Event) -> JsAny?)?
    override var ontoggle: ((Event) -> JsAny?)?
    override var onvolumechange: ((Event) -> JsAny?)?
    override var onwaiting: ((Event) -> JsAny?)?
    override var ongotpointercapture: ((PointerEvent) -> JsAny?)?
    override var onlostpointercapture: ((PointerEvent) -> JsAny?)?
    override var onpointerdown: ((PointerEvent) -> JsAny?)?
    override var onpointermove: ((PointerEvent) -> JsAny?)?
    override var onpointerup: ((PointerEvent) -> JsAny?)?
    override var onpointercancel: ((PointerEvent) -> JsAny?)?
    override var onpointerover: ((PointerEvent) -> JsAny?)?
    override var onpointerout: ((PointerEvent) -> JsAny?)?
    override var onpointerenter: ((PointerEvent) -> JsAny?)?
    override var onpointerleave: ((PointerEvent) -> JsAny?)?
    override var oncopy: ((ClipboardEvent) -> JsAny?)?
    override var oncut: ((ClipboardEvent) -> JsAny?)?
    override var onpaste: ((ClipboardEvent) -> JsAny?)?
    override konst fullscreenElement: Element?
    override konst children: HTMLCollection
    override konst firstElementChild: Element?
    override konst lastElementChild: Element?
    override konst childElementCount: Int
    fun getElementsByTagName(qualifiedName: String): HTMLCollection
    fun getElementsByTagNameNS(namespace: String?, localName: String): HTMLCollection
    fun getElementsByClassName(classNames: String): HTMLCollection
    fun createElement(localName: String, options: ElementCreationOptions = definedExternally): Element
    fun createElementNS(namespace: String?, qualifiedName: String, options: ElementCreationOptions = definedExternally): Element
    fun createDocumentFragment(): DocumentFragment
    fun createTextNode(data: String): Text
    fun createCDATASection(data: String): CDATASection
    fun createComment(data: String): Comment
    fun createProcessingInstruction(target: String, data: String): ProcessingInstruction
    fun importNode(node: Node, deep: Boolean = definedExternally): Node
    fun adoptNode(node: Node): Node
    fun createAttribute(localName: String): Attr
    fun createAttributeNS(namespace: String?, qualifiedName: String): Attr
    fun createEvent(param_interface: String): Event
    fun createRange(): Range
    fun createNodeIterator(root: Node, whatToShow: Int = definedExternally, filter: NodeFilter? = definedExternally): NodeIterator
    fun createNodeIterator(root: Node, whatToShow: Int = definedExternally, filter: ((Node) -> Short)? = definedExternally): NodeIterator
    fun createTreeWalker(root: Node, whatToShow: Int = definedExternally, filter: NodeFilter? = definedExternally): TreeWalker
    fun createTreeWalker(root: Node, whatToShow: Int = definedExternally, filter: ((Node) -> Short)? = definedExternally): TreeWalker
    fun getElementsByName(elementName: String): NodeList
    fun open(type: String = definedExternally, replace: String = definedExternally): Document
    fun open(url: String, name: String, features: String): Window
    fun close()
    fun write(vararg text: String)
    fun writeln(vararg text: String)
    fun hasFocus(): Boolean
    fun execCommand(commandId: String, showUI: Boolean = definedExternally, konstue: String = definedExternally): Boolean
    fun queryCommandEnabled(commandId: String): Boolean
    fun queryCommandIndeterm(commandId: String): Boolean
    fun queryCommandState(commandId: String): Boolean
    fun queryCommandSupported(commandId: String): Boolean
    fun queryCommandValue(commandId: String): String
    fun clear()
    fun captureEvents()
    fun releaseEvents()
    fun elementFromPoint(x: Double, y: Double): Element?
    fun elementsFromPoint(x: Double, y: Double): JsArray<Element>
    fun caretPositionFromPoint(x: Double, y: Double): CaretPosition?
    fun createTouch(view: Window, target: EventTarget, identifier: Int, pageX: Int, pageY: Int, screenX: Int, screenY: Int): Touch
    fun createTouchList(vararg touches: Touch): TouchList
    fun exitFullscreen(): Promise<Nothing?>
    override fun getElementById(elementId: String): Element?
    override fun prepend(vararg nodes: JsAny?)
    override fun append(vararg nodes: JsAny?)
    override fun querySelector(selectors: String): Element?
    override fun querySelectorAll(selectors: String): NodeList
    override fun getBoxQuads(options: BoxQuadOptions /* = definedExternally */): JsArray<DOMQuad>
    override fun convertQuadFromNode(quad: JsAny?, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertRectFromNode(rect: DOMRectReadOnly, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertPointFromNode(point: DOMPointInit, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMPoint

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

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForDocument(obj: Document, name: String): JsAny? { js("return obj[name];") }

public operator fun Document.get(name: String): JsAny? = getMethodImplForDocument(this, name)

/**
 * Exposes the JavaScript [XMLDocument](https://developer.mozilla.org/en/docs/Web/API/XMLDocument) to Kotlin
 */
public external open class XMLDocument : Document, JsAny {
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

public external interface ElementCreationOptions : JsAny {
    var `is`: String?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ElementCreationOptions(param_is: String? = undefined): ElementCreationOptions { js("return { is };") }

/**
 * Exposes the JavaScript [DOMImplementation](https://developer.mozilla.org/en/docs/Web/API/DOMImplementation) to Kotlin
 */
public external abstract class DOMImplementation : JsAny {
    fun createDocumentType(qualifiedName: String, publicId: String, systemId: String): DocumentType
    fun createDocument(namespace: String?, qualifiedName: String, doctype: DocumentType? = definedExternally): XMLDocument
    fun createHTMLDocument(title: String = definedExternally): Document
    fun hasFeature(): Boolean
}

/**
 * Exposes the JavaScript [DocumentType](https://developer.mozilla.org/en/docs/Web/API/DocumentType) to Kotlin
 */
public external abstract class DocumentType : Node, ChildNode, JsAny {
    open konst name: String
    open konst publicId: String
    open konst systemId: String

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
 * Exposes the JavaScript [DocumentFragment](https://developer.mozilla.org/en/docs/Web/API/DocumentFragment) to Kotlin
 */
public external open class DocumentFragment : Node, NonElementParentNode, ParentNode, JsAny {
    override konst children: HTMLCollection
    override konst firstElementChild: Element?
    override konst lastElementChild: Element?
    override konst childElementCount: Int
    override fun getElementById(elementId: String): Element?
    override fun prepend(vararg nodes: JsAny?)
    override fun append(vararg nodes: JsAny?)
    override fun querySelector(selectors: String): Element?
    override fun querySelectorAll(selectors: String): NodeList

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
 * Exposes the JavaScript [ShadowRoot](https://developer.mozilla.org/en/docs/Web/API/ShadowRoot) to Kotlin
 */
public external open class ShadowRoot : DocumentFragment, DocumentOrShadowRoot, JsAny {
    open konst mode: ShadowRootMode
    open konst host: Element
    override konst fullscreenElement: Element?

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
 * Exposes the JavaScript [Element](https://developer.mozilla.org/en/docs/Web/API/Element) to Kotlin
 */
public external abstract class Element : Node, ParentNode, NonDocumentTypeChildNode, ChildNode, Slotable, GeometryUtils, UnionElementOrHTMLCollection, UnionElementOrRadioNodeList, UnionElementOrMouseEvent, UnionElementOrProcessingInstruction, JsAny {
    open konst namespaceURI: String?
    open konst prefix: String?
    open konst localName: String
    open konst tagName: String
    open var id: String
    open var className: String
    open konst classList: DOMTokenList
    open var slot: String
    open konst attributes: NamedNodeMap
    open konst shadowRoot: ShadowRoot?
    open var scrollTop: Double
    open var scrollLeft: Double
    open konst scrollWidth: Int
    open konst scrollHeight: Int
    open konst clientTop: Int
    open konst clientLeft: Int
    open konst clientWidth: Int
    open konst clientHeight: Int
    open var innerHTML: String
    open var outerHTML: String
    fun hasAttributes(): Boolean
    fun getAttributeNames(): JsArray<JsString>
    fun getAttribute(qualifiedName: String): String?
    fun getAttributeNS(namespace: String?, localName: String): String?
    fun setAttribute(qualifiedName: String, konstue: String)
    fun setAttributeNS(namespace: String?, qualifiedName: String, konstue: String)
    fun removeAttribute(qualifiedName: String)
    fun removeAttributeNS(namespace: String?, localName: String)
    fun hasAttribute(qualifiedName: String): Boolean
    fun hasAttributeNS(namespace: String?, localName: String): Boolean
    fun getAttributeNode(qualifiedName: String): Attr?
    fun getAttributeNodeNS(namespace: String?, localName: String): Attr?
    fun setAttributeNode(attr: Attr): Attr?
    fun setAttributeNodeNS(attr: Attr): Attr?
    fun removeAttributeNode(attr: Attr): Attr
    fun attachShadow(init: ShadowRootInit): ShadowRoot
    fun closest(selectors: String): Element?
    fun matches(selectors: String): Boolean
    fun webkitMatchesSelector(selectors: String): Boolean
    fun getElementsByTagName(qualifiedName: String): HTMLCollection
    fun getElementsByTagNameNS(namespace: String?, localName: String): HTMLCollection
    fun getElementsByClassName(classNames: String): HTMLCollection
    fun insertAdjacentElement(where: String, element: Element): Element?
    fun insertAdjacentText(where: String, data: String)
    fun getClientRects(): JsArray<DOMRect>
    fun getBoundingClientRect(): DOMRect
    fun scrollIntoView()
    fun scrollIntoView(arg: JsAny?)
    fun scroll(options: ScrollToOptions = definedExternally)
    fun scroll(x: Double, y: Double)
    fun scrollTo(options: ScrollToOptions = definedExternally)
    fun scrollTo(x: Double, y: Double)
    fun scrollBy(options: ScrollToOptions = definedExternally)
    fun scrollBy(x: Double, y: Double)
    fun insertAdjacentHTML(position: String, text: String)
    fun setPointerCapture(pointerId: Int)
    fun releasePointerCapture(pointerId: Int)
    fun hasPointerCapture(pointerId: Int): Boolean
    fun requestFullscreen(): Promise<Nothing?>

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

public external interface ShadowRootInit : JsAny {
    var mode: ShadowRootMode?
}

@Suppress("UNUSED_PARAMETER")
public fun ShadowRootInit(mode: ShadowRootMode?): ShadowRootInit { js("return { mode };") }

/**
 * Exposes the JavaScript [NamedNodeMap](https://developer.mozilla.org/en/docs/Web/API/NamedNodeMap) to Kotlin
 */
public external abstract class NamedNodeMap : ItemArrayLike<Attr>, JsAny {
    fun getNamedItemNS(namespace: String?, localName: String): Attr?
    fun setNamedItem(attr: Attr): Attr?
    fun setNamedItemNS(attr: Attr): Attr?
    fun removeNamedItem(qualifiedName: String): Attr
    fun removeNamedItemNS(namespace: String?, localName: String): Attr
    override fun item(index: Int): Attr?
    fun getNamedItem(qualifiedName: String): Attr?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForNamedNodeMap(obj: NamedNodeMap, index: Int): Attr? { js("return obj[index];") }

public operator fun NamedNodeMap.get(index: Int): Attr? = getMethodImplForNamedNodeMap(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForNamedNodeMap(obj: NamedNodeMap, qualifiedName: String): Attr? { js("return obj[qualifiedName];") }

public operator fun NamedNodeMap.get(qualifiedName: String): Attr? = getMethodImplForNamedNodeMap(this, qualifiedName)

/**
 * Exposes the JavaScript [Attr](https://developer.mozilla.org/en/docs/Web/API/Attr) to Kotlin
 */
public external abstract class Attr : Node, JsAny {
    open konst namespaceURI: String?
    open konst prefix: String?
    open konst localName: String
    open konst name: String
    open var konstue: String
    open konst ownerElement: Element?
    open konst specified: Boolean

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
 * Exposes the JavaScript [CharacterData](https://developer.mozilla.org/en/docs/Web/API/CharacterData) to Kotlin
 */
public external abstract class CharacterData : Node, NonDocumentTypeChildNode, ChildNode, JsAny {
    open var data: String
    open konst length: Int
    fun substringData(offset: Int, count: Int): String
    fun appendData(data: String)
    fun insertData(offset: Int, data: String)
    fun deleteData(offset: Int, count: Int)
    fun replaceData(offset: Int, count: Int, data: String)

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
 * Exposes the JavaScript [Text](https://developer.mozilla.org/en/docs/Web/API/Text) to Kotlin
 */
public external open class Text(data: String = definedExternally) : CharacterData, Slotable, GeometryUtils, JsAny {
    open konst wholeText: String
    override konst assignedSlot: HTMLSlotElement?
    override konst previousElementSibling: Element?
    override konst nextElementSibling: Element?
    fun splitText(offset: Int): Text
    override fun getBoxQuads(options: BoxQuadOptions /* = definedExternally */): JsArray<DOMQuad>
    override fun convertQuadFromNode(quad: JsAny?, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertRectFromNode(rect: DOMRectReadOnly, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertPointFromNode(point: DOMPointInit, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMPoint
    override fun before(vararg nodes: JsAny?)
    override fun after(vararg nodes: JsAny?)
    override fun replaceWith(vararg nodes: JsAny?)
    override fun remove()

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
 * Exposes the JavaScript [CDATASection](https://developer.mozilla.org/en/docs/Web/API/CDATASection) to Kotlin
 */
public external open class CDATASection : Text, JsAny {
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
 * Exposes the JavaScript [ProcessingInstruction](https://developer.mozilla.org/en/docs/Web/API/ProcessingInstruction) to Kotlin
 */
public external abstract class ProcessingInstruction : CharacterData, LinkStyle, UnionElementOrProcessingInstruction, JsAny {
    open konst target: String

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
 * Exposes the JavaScript [Comment](https://developer.mozilla.org/en/docs/Web/API/Comment) to Kotlin
 */
public external open class Comment(data: String = definedExternally) : CharacterData, JsAny {
    override konst previousElementSibling: Element?
    override konst nextElementSibling: Element?
    override fun before(vararg nodes: JsAny?)
    override fun after(vararg nodes: JsAny?)
    override fun replaceWith(vararg nodes: JsAny?)
    override fun remove()

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
 * Exposes the JavaScript [Range](https://developer.mozilla.org/en/docs/Web/API/Range) to Kotlin
 */
public external open class Range : JsAny {
    open konst startContainer: Node
    open konst startOffset: Int
    open konst endContainer: Node
    open konst endOffset: Int
    open konst collapsed: Boolean
    open konst commonAncestorContainer: Node
    fun setStart(node: Node, offset: Int)
    fun setEnd(node: Node, offset: Int)
    fun setStartBefore(node: Node)
    fun setStartAfter(node: Node)
    fun setEndBefore(node: Node)
    fun setEndAfter(node: Node)
    fun collapse(toStart: Boolean = definedExternally)
    fun selectNode(node: Node)
    fun selectNodeContents(node: Node)
    fun compareBoundaryPoints(how: Short, sourceRange: Range): Short
    fun deleteContents()
    fun extractContents(): DocumentFragment
    fun cloneContents(): DocumentFragment
    fun insertNode(node: Node)
    fun surroundContents(newParent: Node)
    fun cloneRange(): Range
    fun detach()
    fun isPointInRange(node: Node, offset: Int): Boolean
    fun comparePoint(node: Node, offset: Int): Short
    fun intersectsNode(node: Node): Boolean
    fun getClientRects(): JsArray<DOMRect>
    fun getBoundingClientRect(): DOMRect
    fun createContextualFragment(fragment: String): DocumentFragment

    companion object {
        konst START_TO_START: Short
        konst START_TO_END: Short
        konst END_TO_END: Short
        konst END_TO_START: Short
    }
}

/**
 * Exposes the JavaScript [NodeIterator](https://developer.mozilla.org/en/docs/Web/API/NodeIterator) to Kotlin
 */
public external abstract class NodeIterator : JsAny {
    open konst root: Node
    open konst referenceNode: Node
    open konst pointerBeforeReferenceNode: Boolean
    open konst whatToShow: Int
    open konst filter: NodeFilter?
    fun nextNode(): Node?
    fun previousNode(): Node?
    fun detach()
}

/**
 * Exposes the JavaScript [TreeWalker](https://developer.mozilla.org/en/docs/Web/API/TreeWalker) to Kotlin
 */
public external abstract class TreeWalker : JsAny {
    open konst root: Node
    open konst whatToShow: Int
    open konst filter: NodeFilter?
    open var currentNode: Node
    fun parentNode(): Node?
    fun firstChild(): Node?
    fun lastChild(): Node?
    fun previousSibling(): Node?
    fun nextSibling(): Node?
    fun previousNode(): Node?
    fun nextNode(): Node?
}

/**
 * Exposes the JavaScript [NodeFilter](https://developer.mozilla.org/en/docs/Web/API/NodeFilter) to Kotlin
 */
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface NodeFilter : JsAny {
    fun acceptNode(node: Node): Short

    companion object {
        konst FILTER_ACCEPT: Short
        konst FILTER_REJECT: Short
        konst FILTER_SKIP: Short
        konst SHOW_ALL: Int
        konst SHOW_ELEMENT: Int
        konst SHOW_ATTRIBUTE: Int
        konst SHOW_TEXT: Int
        konst SHOW_CDATA_SECTION: Int
        konst SHOW_ENTITY_REFERENCE: Int
        konst SHOW_ENTITY: Int
        konst SHOW_PROCESSING_INSTRUCTION: Int
        konst SHOW_COMMENT: Int
        konst SHOW_DOCUMENT: Int
        konst SHOW_DOCUMENT_TYPE: Int
        konst SHOW_DOCUMENT_FRAGMENT: Int
        konst SHOW_NOTATION: Int
    }
}

/**
 * Exposes the JavaScript [DOMTokenList](https://developer.mozilla.org/en/docs/Web/API/DOMTokenList) to Kotlin
 */
public external abstract class DOMTokenList : ItemArrayLike<JsString>, JsAny {
    open var konstue: String
    fun contains(token: String): Boolean
    fun add(vararg tokens: String)
    fun remove(vararg tokens: String)
    fun toggle(token: String, force: Boolean = definedExternally): Boolean
    fun replace(token: String, newToken: String)
    fun supports(token: String): Boolean
    override fun item(index: Int): JsString?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForDOMTokenList(obj: DOMTokenList, index: Int): String? { js("return obj[index];") }

public operator fun DOMTokenList.get(index: Int): String? = getMethodImplForDOMTokenList(this, index)

/**
 * Exposes the JavaScript [DOMPointReadOnly](https://developer.mozilla.org/en/docs/Web/API/DOMPointReadOnly) to Kotlin
 */
public external open class DOMPointReadOnly(x: Double, y: Double, z: Double, w: Double) : JsAny {
    open konst x: Double
    open konst y: Double
    open konst z: Double
    open konst w: Double
    fun matrixTransform(matrix: DOMMatrixReadOnly): DOMPoint
}

/**
 * Exposes the JavaScript [DOMPoint](https://developer.mozilla.org/en/docs/Web/API/DOMPoint) to Kotlin
 */
public external open class DOMPoint : DOMPointReadOnly, JsAny {
    constructor(point: DOMPointInit)
    constructor(x: Double = definedExternally, y: Double = definedExternally, z: Double = definedExternally, w: Double = definedExternally)
    override var x: Double
    override var y: Double
    override var z: Double
    override var w: Double
}

/**
 * Exposes the JavaScript [DOMPointInit](https://developer.mozilla.org/en/docs/Web/API/DOMPointInit) to Kotlin
 */
public external interface DOMPointInit : JsAny {
    var x: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var y: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var z: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var w: Double? /* = 1.0 */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun DOMPointInit(x: Double? = 0.0, y: Double? = 0.0, z: Double? = 0.0, w: Double? = 1.0): DOMPointInit { js("return { x, y, z, w };") }

/**
 * Exposes the JavaScript [DOMRect](https://developer.mozilla.org/en/docs/Web/API/DOMRect) to Kotlin
 */
public external open class DOMRect(x: Double = definedExternally, y: Double = definedExternally, width: Double = definedExternally, height: Double = definedExternally) : DOMRectReadOnly, JsAny {
    override var x: Double
    override var y: Double
    override var width: Double
    override var height: Double
}

/**
 * Exposes the JavaScript [DOMRectReadOnly](https://developer.mozilla.org/en/docs/Web/API/DOMRectReadOnly) to Kotlin
 */
public external open class DOMRectReadOnly(x: Double, y: Double, width: Double, height: Double) : JsAny {
    open konst x: Double
    open konst y: Double
    open konst width: Double
    open konst height: Double
    open konst top: Double
    open konst right: Double
    open konst bottom: Double
    open konst left: Double
}

public external interface DOMRectInit : JsAny {
    var x: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var y: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var width: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
    var height: Double? /* = 0.0 */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun DOMRectInit(x: Double? = 0.0, y: Double? = 0.0, width: Double? = 0.0, height: Double? = 0.0): DOMRectInit { js("return { x, y, width, height };") }

public external interface DOMRectList : ItemArrayLike<DOMRect>, JsAny {
    override fun item(index: Int): DOMRect?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForDOMRectList(obj: DOMRectList, index: Int): DOMRect? { js("return obj[index];") }

public operator fun DOMRectList.get(index: Int): DOMRect? = getMethodImplForDOMRectList(this, index)

/**
 * Exposes the JavaScript [DOMQuad](https://developer.mozilla.org/en/docs/Web/API/DOMQuad) to Kotlin
 */
public external open class DOMQuad : JsAny {
    constructor(p1: DOMPointInit = definedExternally, p2: DOMPointInit = definedExternally, p3: DOMPointInit = definedExternally, p4: DOMPointInit = definedExternally)
    constructor(rect: DOMRectInit)
    open konst p1: DOMPoint
    open konst p2: DOMPoint
    open konst p3: DOMPoint
    open konst p4: DOMPoint
    open konst bounds: DOMRectReadOnly
}

/**
 * Exposes the JavaScript [DOMMatrixReadOnly](https://developer.mozilla.org/en/docs/Web/API/DOMMatrixReadOnly) to Kotlin
 */
public external open class DOMMatrixReadOnly(numberSequence: JsArray<JsNumber>) : JsAny {
    open konst a: Double
    open konst b: Double
    open konst c: Double
    open konst d: Double
    open konst e: Double
    open konst f: Double
    open konst m11: Double
    open konst m12: Double
    open konst m13: Double
    open konst m14: Double
    open konst m21: Double
    open konst m22: Double
    open konst m23: Double
    open konst m24: Double
    open konst m31: Double
    open konst m32: Double
    open konst m33: Double
    open konst m34: Double
    open konst m41: Double
    open konst m42: Double
    open konst m43: Double
    open konst m44: Double
    open konst is2D: Boolean
    open konst isIdentity: Boolean
    fun translate(tx: Double, ty: Double, tz: Double = definedExternally): DOMMatrix
    fun scale(scale: Double, originX: Double = definedExternally, originY: Double = definedExternally): DOMMatrix
    fun scale3d(scale: Double, originX: Double = definedExternally, originY: Double = definedExternally, originZ: Double = definedExternally): DOMMatrix
    fun scaleNonUniform(scaleX: Double, scaleY: Double = definedExternally, scaleZ: Double = definedExternally, originX: Double = definedExternally, originY: Double = definedExternally, originZ: Double = definedExternally): DOMMatrix
    fun rotate(angle: Double, originX: Double = definedExternally, originY: Double = definedExternally): DOMMatrix
    fun rotateFromVector(x: Double, y: Double): DOMMatrix
    fun rotateAxisAngle(x: Double, y: Double, z: Double, angle: Double): DOMMatrix
    fun skewX(sx: Double): DOMMatrix
    fun skewY(sy: Double): DOMMatrix
    fun multiply(other: DOMMatrix): DOMMatrix
    fun flipX(): DOMMatrix
    fun flipY(): DOMMatrix
    fun inverse(): DOMMatrix
    fun transformPoint(point: DOMPointInit = definedExternally): DOMPoint
    fun toFloat32Array(): Float32Array
    fun toFloat64Array(): Float64Array
}

/**
 * Exposes the JavaScript [DOMMatrix](https://developer.mozilla.org/en/docs/Web/API/DOMMatrix) to Kotlin
 */
public external open class DOMMatrix() : DOMMatrixReadOnly, JsAny {
    constructor(transformList: String)
    constructor(other: DOMMatrixReadOnly)
    constructor(array32: Float32Array)
    constructor(array64: Float64Array)
    constructor(numberSequence: JsArray<JsNumber>)
    override var a: Double
    override var b: Double
    override var c: Double
    override var d: Double
    override var e: Double
    override var f: Double
    override var m11: Double
    override var m12: Double
    override var m13: Double
    override var m14: Double
    override var m21: Double
    override var m22: Double
    override var m23: Double
    override var m24: Double
    override var m31: Double
    override var m32: Double
    override var m33: Double
    override var m34: Double
    override var m41: Double
    override var m42: Double
    override var m43: Double
    override var m44: Double
    fun multiplySelf(other: DOMMatrix): DOMMatrix
    fun preMultiplySelf(other: DOMMatrix): DOMMatrix
    fun translateSelf(tx: Double, ty: Double, tz: Double = definedExternally): DOMMatrix
    fun scaleSelf(scale: Double, originX: Double = definedExternally, originY: Double = definedExternally): DOMMatrix
    fun scale3dSelf(scale: Double, originX: Double = definedExternally, originY: Double = definedExternally, originZ: Double = definedExternally): DOMMatrix
    fun scaleNonUniformSelf(scaleX: Double, scaleY: Double = definedExternally, scaleZ: Double = definedExternally, originX: Double = definedExternally, originY: Double = definedExternally, originZ: Double = definedExternally): DOMMatrix
    fun rotateSelf(angle: Double, originX: Double = definedExternally, originY: Double = definedExternally): DOMMatrix
    fun rotateFromVectorSelf(x: Double, y: Double): DOMMatrix
    fun rotateAxisAngleSelf(x: Double, y: Double, z: Double, angle: Double): DOMMatrix
    fun skewXSelf(sx: Double): DOMMatrix
    fun skewYSelf(sy: Double): DOMMatrix
    fun invertSelf(): DOMMatrix
    fun setMatrixValue(transformList: String): DOMMatrix
}

public external interface ScrollOptions : JsAny {
    var behavior: ScrollBehavior? /* = ScrollBehavior.AUTO */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ScrollOptions(behavior: ScrollBehavior? = ScrollBehavior.AUTO): ScrollOptions { js("return { behavior };") }

/**
 * Exposes the JavaScript [ScrollToOptions](https://developer.mozilla.org/en/docs/Web/API/ScrollToOptions) to Kotlin
 */
public external interface ScrollToOptions : ScrollOptions, JsAny {
    var left: Double?
        get() = definedExternally
        set(konstue) = definedExternally
    var top: Double?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ScrollToOptions(left: Double? = undefined, top: Double? = undefined, behavior: ScrollBehavior? = ScrollBehavior.AUTO): ScrollToOptions { js("return { left, top, behavior };") }

/**
 * Exposes the JavaScript [MediaQueryList](https://developer.mozilla.org/en/docs/Web/API/MediaQueryList) to Kotlin
 */
public external abstract class MediaQueryList : EventTarget, JsAny {
    open konst media: String
    open konst matches: Boolean
    open var onchange: ((Event) -> JsAny?)?
    fun addListener(listener: EventListener?)
    fun addListener(listener: ((Event) -> Unit)?)
    fun removeListener(listener: EventListener?)
    fun removeListener(listener: ((Event) -> Unit)?)
}

/**
 * Exposes the JavaScript [MediaQueryListEvent](https://developer.mozilla.org/en/docs/Web/API/MediaQueryListEvent) to Kotlin
 */
public external open class MediaQueryListEvent(type: String, eventInitDict: MediaQueryListEventInit = definedExternally) : Event, JsAny {
    open konst media: String
    open konst matches: Boolean

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface MediaQueryListEventInit : EventInit, JsAny {
    var media: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
    var matches: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun MediaQueryListEventInit(media: String? = "", matches: Boolean? = false, bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): MediaQueryListEventInit { js("return { media, matches, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [Screen](https://developer.mozilla.org/en/docs/Web/API/Screen) to Kotlin
 */
public external abstract class Screen : JsAny {
    open konst availWidth: Int
    open konst availHeight: Int
    open konst width: Int
    open konst height: Int
    open konst colorDepth: Int
    open konst pixelDepth: Int
}

/**
 * Exposes the JavaScript [CaretPosition](https://developer.mozilla.org/en/docs/Web/API/CaretPosition) to Kotlin
 */
public external abstract class CaretPosition : JsAny {
    open konst offsetNode: Node
    open konst offset: Int
    fun getClientRect(): DOMRect?
}

public external interface ScrollIntoViewOptions : ScrollOptions, JsAny {
    var block: ScrollLogicalPosition? /* = ScrollLogicalPosition.CENTER */
        get() = definedExternally
        set(konstue) = definedExternally
    var inline: ScrollLogicalPosition? /* = ScrollLogicalPosition.CENTER */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ScrollIntoViewOptions(block: ScrollLogicalPosition? = ScrollLogicalPosition.CENTER, inline: ScrollLogicalPosition? = ScrollLogicalPosition.CENTER, behavior: ScrollBehavior? = ScrollBehavior.AUTO): ScrollIntoViewOptions { js("return { block, inline, behavior };") }

public external interface BoxQuadOptions : JsAny {
    var box: CSSBoxType? /* = CSSBoxType.BORDER */
        get() = definedExternally
        set(konstue) = definedExternally
    var relativeTo: JsAny?
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun BoxQuadOptions(box: CSSBoxType? = CSSBoxType.BORDER, relativeTo: JsAny? = undefined): BoxQuadOptions { js("return { box, relativeTo };") }

public external interface ConvertCoordinateOptions : JsAny {
    var fromBox: CSSBoxType? /* = CSSBoxType.BORDER */
        get() = definedExternally
        set(konstue) = definedExternally
    var toBox: CSSBoxType? /* = CSSBoxType.BORDER */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun ConvertCoordinateOptions(fromBox: CSSBoxType? = CSSBoxType.BORDER, toBox: CSSBoxType? = CSSBoxType.BORDER): ConvertCoordinateOptions { js("return { fromBox, toBox };") }

/**
 * Exposes the JavaScript [GeometryUtils](https://developer.mozilla.org/en/docs/Web/API/GeometryUtils) to Kotlin
 */
public external interface GeometryUtils : JsAny {
    fun getBoxQuads(options: BoxQuadOptions = definedExternally): JsArray<DOMQuad>
    fun convertQuadFromNode(quad: JsAny?, from: JsAny?, options: ConvertCoordinateOptions = definedExternally): DOMQuad
    fun convertRectFromNode(rect: DOMRectReadOnly, from: JsAny?, options: ConvertCoordinateOptions = definedExternally): DOMQuad
    fun convertPointFromNode(point: DOMPointInit, from: JsAny?, options: ConvertCoordinateOptions = definedExternally): DOMPoint
}

/**
 * Exposes the JavaScript [Touch](https://developer.mozilla.org/en/docs/Web/API/Touch) to Kotlin
 */
public external abstract class Touch : JsAny {
    open konst identifier: Int
    open konst target: EventTarget
    open konst screenX: Int
    open konst screenY: Int
    open konst clientX: Int
    open konst clientY: Int
    open konst pageX: Int
    open konst pageY: Int
    open konst region: String?
}

public external abstract class TouchList : ItemArrayLike<Touch>, JsAny {
    override fun item(index: Int): Touch?
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForTouchList(obj: TouchList, index: Int): Touch? { js("return obj[index];") }

public operator fun TouchList.get(index: Int): Touch? = getMethodImplForTouchList(this, index)

public external open class TouchEvent : UIEvent, JsAny {
    open konst touches: TouchList
    open konst targetTouches: TouchList
    open konst changedTouches: TouchList
    open konst altKey: Boolean
    open konst metaKey: Boolean
    open konst ctrlKey: Boolean
    open konst shiftKey: Boolean

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

/**
 * Exposes the JavaScript [Image](https://developer.mozilla.org/en/docs/Web/API/Image) to Kotlin
 */
public external open class Image(width: Int = definedExternally, height: Int = definedExternally) : HTMLImageElement, JsAny {
    override var onabort: ((Event) -> JsAny?)?
    override var onblur: ((FocusEvent) -> JsAny?)?
    override var oncancel: ((Event) -> JsAny?)?
    override var oncanplay: ((Event) -> JsAny?)?
    override var oncanplaythrough: ((Event) -> JsAny?)?
    override var onchange: ((Event) -> JsAny?)?
    override var onclick: ((MouseEvent) -> JsAny?)?
    override var onclose: ((Event) -> JsAny?)?
    override var oncontextmenu: ((MouseEvent) -> JsAny?)?
    override var oncuechange: ((Event) -> JsAny?)?
    override var ondblclick: ((MouseEvent) -> JsAny?)?
    override var ondrag: ((DragEvent) -> JsAny?)?
    override var ondragend: ((DragEvent) -> JsAny?)?
    override var ondragenter: ((DragEvent) -> JsAny?)?
    override var ondragexit: ((DragEvent) -> JsAny?)?
    override var ondragleave: ((DragEvent) -> JsAny?)?
    override var ondragover: ((DragEvent) -> JsAny?)?
    override var ondragstart: ((DragEvent) -> JsAny?)?
    override var ondrop: ((DragEvent) -> JsAny?)?
    override var ondurationchange: ((Event) -> JsAny?)?
    override var onemptied: ((Event) -> JsAny?)?
    override var onended: ((Event) -> JsAny?)?
    override var onerror: ((JsAny?, String, Int, Int, JsAny?) -> JsAny?)?
    override var onfocus: ((FocusEvent) -> JsAny?)?
    override var oninput: ((InputEvent) -> JsAny?)?
    override var oninkonstid: ((Event) -> JsAny?)?
    override var onkeydown: ((KeyboardEvent) -> JsAny?)?
    override var onkeypress: ((KeyboardEvent) -> JsAny?)?
    override var onkeyup: ((KeyboardEvent) -> JsAny?)?
    override var onload: ((Event) -> JsAny?)?
    override var onloadeddata: ((Event) -> JsAny?)?
    override var onloadedmetadata: ((Event) -> JsAny?)?
    override var onloadend: ((Event) -> JsAny?)?
    override var onloadstart: ((ProgressEvent) -> JsAny?)?
    override var onmousedown: ((MouseEvent) -> JsAny?)?
    override var onmouseenter: ((MouseEvent) -> JsAny?)?
    override var onmouseleave: ((MouseEvent) -> JsAny?)?
    override var onmousemove: ((MouseEvent) -> JsAny?)?
    override var onmouseout: ((MouseEvent) -> JsAny?)?
    override var onmouseover: ((MouseEvent) -> JsAny?)?
    override var onmouseup: ((MouseEvent) -> JsAny?)?
    override var onwheel: ((WheelEvent) -> JsAny?)?
    override var onpause: ((Event) -> JsAny?)?
    override var onplay: ((Event) -> JsAny?)?
    override var onplaying: ((Event) -> JsAny?)?
    override var onprogress: ((ProgressEvent) -> JsAny?)?
    override var onratechange: ((Event) -> JsAny?)?
    override var onreset: ((Event) -> JsAny?)?
    override var onresize: ((Event) -> JsAny?)?
    override var onscroll: ((Event) -> JsAny?)?
    override var onseeked: ((Event) -> JsAny?)?
    override var onseeking: ((Event) -> JsAny?)?
    override var onselect: ((Event) -> JsAny?)?
    override var onshow: ((Event) -> JsAny?)?
    override var onstalled: ((Event) -> JsAny?)?
    override var onsubmit: ((Event) -> JsAny?)?
    override var onsuspend: ((Event) -> JsAny?)?
    override var ontimeupdate: ((Event) -> JsAny?)?
    override var ontoggle: ((Event) -> JsAny?)?
    override var onvolumechange: ((Event) -> JsAny?)?
    override var onwaiting: ((Event) -> JsAny?)?
    override var ongotpointercapture: ((PointerEvent) -> JsAny?)?
    override var onlostpointercapture: ((PointerEvent) -> JsAny?)?
    override var onpointerdown: ((PointerEvent) -> JsAny?)?
    override var onpointermove: ((PointerEvent) -> JsAny?)?
    override var onpointerup: ((PointerEvent) -> JsAny?)?
    override var onpointercancel: ((PointerEvent) -> JsAny?)?
    override var onpointerover: ((PointerEvent) -> JsAny?)?
    override var onpointerout: ((PointerEvent) -> JsAny?)?
    override var onpointerenter: ((PointerEvent) -> JsAny?)?
    override var onpointerleave: ((PointerEvent) -> JsAny?)?
    override var oncopy: ((ClipboardEvent) -> JsAny?)?
    override var oncut: ((ClipboardEvent) -> JsAny?)?
    override var onpaste: ((ClipboardEvent) -> JsAny?)?
    override var contentEditable: String
    override konst isContentEditable: Boolean
    override konst style: CSSStyleDeclaration
    override konst children: HTMLCollection
    override konst firstElementChild: Element?
    override konst lastElementChild: Element?
    override konst childElementCount: Int
    override konst previousElementSibling: Element?
    override konst nextElementSibling: Element?
    override konst assignedSlot: HTMLSlotElement?
    override fun prepend(vararg nodes: JsAny?)
    override fun append(vararg nodes: JsAny?)
    override fun querySelector(selectors: String): Element?
    override fun querySelectorAll(selectors: String): NodeList
    override fun before(vararg nodes: JsAny?)
    override fun after(vararg nodes: JsAny?)
    override fun replaceWith(vararg nodes: JsAny?)
    override fun remove()
    override fun getBoxQuads(options: BoxQuadOptions /* = definedExternally */): JsArray<DOMQuad>
    override fun convertQuadFromNode(quad: JsAny?, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertRectFromNode(rect: DOMRectReadOnly, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertPointFromNode(point: DOMPointInit, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMPoint

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

public external open class Audio(src: String = definedExternally) : HTMLAudioElement, JsAny {
    override var onabort: ((Event) -> JsAny?)?
    override var onblur: ((FocusEvent) -> JsAny?)?
    override var oncancel: ((Event) -> JsAny?)?
    override var oncanplay: ((Event) -> JsAny?)?
    override var oncanplaythrough: ((Event) -> JsAny?)?
    override var onchange: ((Event) -> JsAny?)?
    override var onclick: ((MouseEvent) -> JsAny?)?
    override var onclose: ((Event) -> JsAny?)?
    override var oncontextmenu: ((MouseEvent) -> JsAny?)?
    override var oncuechange: ((Event) -> JsAny?)?
    override var ondblclick: ((MouseEvent) -> JsAny?)?
    override var ondrag: ((DragEvent) -> JsAny?)?
    override var ondragend: ((DragEvent) -> JsAny?)?
    override var ondragenter: ((DragEvent) -> JsAny?)?
    override var ondragexit: ((DragEvent) -> JsAny?)?
    override var ondragleave: ((DragEvent) -> JsAny?)?
    override var ondragover: ((DragEvent) -> JsAny?)?
    override var ondragstart: ((DragEvent) -> JsAny?)?
    override var ondrop: ((DragEvent) -> JsAny?)?
    override var ondurationchange: ((Event) -> JsAny?)?
    override var onemptied: ((Event) -> JsAny?)?
    override var onended: ((Event) -> JsAny?)?
    override var onerror: ((JsAny?, String, Int, Int, JsAny?) -> JsAny?)?
    override var onfocus: ((FocusEvent) -> JsAny?)?
    override var oninput: ((InputEvent) -> JsAny?)?
    override var oninkonstid: ((Event) -> JsAny?)?
    override var onkeydown: ((KeyboardEvent) -> JsAny?)?
    override var onkeypress: ((KeyboardEvent) -> JsAny?)?
    override var onkeyup: ((KeyboardEvent) -> JsAny?)?
    override var onload: ((Event) -> JsAny?)?
    override var onloadeddata: ((Event) -> JsAny?)?
    override var onloadedmetadata: ((Event) -> JsAny?)?
    override var onloadend: ((Event) -> JsAny?)?
    override var onloadstart: ((ProgressEvent) -> JsAny?)?
    override var onmousedown: ((MouseEvent) -> JsAny?)?
    override var onmouseenter: ((MouseEvent) -> JsAny?)?
    override var onmouseleave: ((MouseEvent) -> JsAny?)?
    override var onmousemove: ((MouseEvent) -> JsAny?)?
    override var onmouseout: ((MouseEvent) -> JsAny?)?
    override var onmouseover: ((MouseEvent) -> JsAny?)?
    override var onmouseup: ((MouseEvent) -> JsAny?)?
    override var onwheel: ((WheelEvent) -> JsAny?)?
    override var onpause: ((Event) -> JsAny?)?
    override var onplay: ((Event) -> JsAny?)?
    override var onplaying: ((Event) -> JsAny?)?
    override var onprogress: ((ProgressEvent) -> JsAny?)?
    override var onratechange: ((Event) -> JsAny?)?
    override var onreset: ((Event) -> JsAny?)?
    override var onresize: ((Event) -> JsAny?)?
    override var onscroll: ((Event) -> JsAny?)?
    override var onseeked: ((Event) -> JsAny?)?
    override var onseeking: ((Event) -> JsAny?)?
    override var onselect: ((Event) -> JsAny?)?
    override var onshow: ((Event) -> JsAny?)?
    override var onstalled: ((Event) -> JsAny?)?
    override var onsubmit: ((Event) -> JsAny?)?
    override var onsuspend: ((Event) -> JsAny?)?
    override var ontimeupdate: ((Event) -> JsAny?)?
    override var ontoggle: ((Event) -> JsAny?)?
    override var onvolumechange: ((Event) -> JsAny?)?
    override var onwaiting: ((Event) -> JsAny?)?
    override var ongotpointercapture: ((PointerEvent) -> JsAny?)?
    override var onlostpointercapture: ((PointerEvent) -> JsAny?)?
    override var onpointerdown: ((PointerEvent) -> JsAny?)?
    override var onpointermove: ((PointerEvent) -> JsAny?)?
    override var onpointerup: ((PointerEvent) -> JsAny?)?
    override var onpointercancel: ((PointerEvent) -> JsAny?)?
    override var onpointerover: ((PointerEvent) -> JsAny?)?
    override var onpointerout: ((PointerEvent) -> JsAny?)?
    override var onpointerenter: ((PointerEvent) -> JsAny?)?
    override var onpointerleave: ((PointerEvent) -> JsAny?)?
    override var oncopy: ((ClipboardEvent) -> JsAny?)?
    override var oncut: ((ClipboardEvent) -> JsAny?)?
    override var onpaste: ((ClipboardEvent) -> JsAny?)?
    override var contentEditable: String
    override konst isContentEditable: Boolean
    override konst style: CSSStyleDeclaration
    override konst children: HTMLCollection
    override konst firstElementChild: Element?
    override konst lastElementChild: Element?
    override konst childElementCount: Int
    override konst previousElementSibling: Element?
    override konst nextElementSibling: Element?
    override konst assignedSlot: HTMLSlotElement?
    override fun prepend(vararg nodes: JsAny?)
    override fun append(vararg nodes: JsAny?)
    override fun querySelector(selectors: String): Element?
    override fun querySelectorAll(selectors: String): NodeList
    override fun before(vararg nodes: JsAny?)
    override fun after(vararg nodes: JsAny?)
    override fun replaceWith(vararg nodes: JsAny?)
    override fun remove()
    override fun getBoxQuads(options: BoxQuadOptions /* = definedExternally */): JsArray<DOMQuad>
    override fun convertQuadFromNode(quad: JsAny?, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertRectFromNode(rect: DOMRectReadOnly, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertPointFromNode(point: DOMPointInit, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMPoint

    companion object {
        konst NETWORK_EMPTY: Short
        konst NETWORK_IDLE: Short
        konst NETWORK_LOADING: Short
        konst NETWORK_NO_SOURCE: Short
        konst HAVE_NOTHING: Short
        konst HAVE_METADATA: Short
        konst HAVE_CURRENT_DATA: Short
        konst HAVE_FUTURE_DATA: Short
        konst HAVE_ENOUGH_DATA: Short
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
 * Exposes the JavaScript [Option](https://developer.mozilla.org/en/docs/Web/API/Option) to Kotlin
 */
public external open class Option(text: String = definedExternally, konstue: String = definedExternally, defaultSelected: Boolean = definedExternally, selected: Boolean = definedExternally) : HTMLOptionElement, JsAny {
    override var onabort: ((Event) -> JsAny?)?
    override var onblur: ((FocusEvent) -> JsAny?)?
    override var oncancel: ((Event) -> JsAny?)?
    override var oncanplay: ((Event) -> JsAny?)?
    override var oncanplaythrough: ((Event) -> JsAny?)?
    override var onchange: ((Event) -> JsAny?)?
    override var onclick: ((MouseEvent) -> JsAny?)?
    override var onclose: ((Event) -> JsAny?)?
    override var oncontextmenu: ((MouseEvent) -> JsAny?)?
    override var oncuechange: ((Event) -> JsAny?)?
    override var ondblclick: ((MouseEvent) -> JsAny?)?
    override var ondrag: ((DragEvent) -> JsAny?)?
    override var ondragend: ((DragEvent) -> JsAny?)?
    override var ondragenter: ((DragEvent) -> JsAny?)?
    override var ondragexit: ((DragEvent) -> JsAny?)?
    override var ondragleave: ((DragEvent) -> JsAny?)?
    override var ondragover: ((DragEvent) -> JsAny?)?
    override var ondragstart: ((DragEvent) -> JsAny?)?
    override var ondrop: ((DragEvent) -> JsAny?)?
    override var ondurationchange: ((Event) -> JsAny?)?
    override var onemptied: ((Event) -> JsAny?)?
    override var onended: ((Event) -> JsAny?)?
    override var onerror: ((JsAny?, String, Int, Int, JsAny?) -> JsAny?)?
    override var onfocus: ((FocusEvent) -> JsAny?)?
    override var oninput: ((InputEvent) -> JsAny?)?
    override var oninkonstid: ((Event) -> JsAny?)?
    override var onkeydown: ((KeyboardEvent) -> JsAny?)?
    override var onkeypress: ((KeyboardEvent) -> JsAny?)?
    override var onkeyup: ((KeyboardEvent) -> JsAny?)?
    override var onload: ((Event) -> JsAny?)?
    override var onloadeddata: ((Event) -> JsAny?)?
    override var onloadedmetadata: ((Event) -> JsAny?)?
    override var onloadend: ((Event) -> JsAny?)?
    override var onloadstart: ((ProgressEvent) -> JsAny?)?
    override var onmousedown: ((MouseEvent) -> JsAny?)?
    override var onmouseenter: ((MouseEvent) -> JsAny?)?
    override var onmouseleave: ((MouseEvent) -> JsAny?)?
    override var onmousemove: ((MouseEvent) -> JsAny?)?
    override var onmouseout: ((MouseEvent) -> JsAny?)?
    override var onmouseover: ((MouseEvent) -> JsAny?)?
    override var onmouseup: ((MouseEvent) -> JsAny?)?
    override var onwheel: ((WheelEvent) -> JsAny?)?
    override var onpause: ((Event) -> JsAny?)?
    override var onplay: ((Event) -> JsAny?)?
    override var onplaying: ((Event) -> JsAny?)?
    override var onprogress: ((ProgressEvent) -> JsAny?)?
    override var onratechange: ((Event) -> JsAny?)?
    override var onreset: ((Event) -> JsAny?)?
    override var onresize: ((Event) -> JsAny?)?
    override var onscroll: ((Event) -> JsAny?)?
    override var onseeked: ((Event) -> JsAny?)?
    override var onseeking: ((Event) -> JsAny?)?
    override var onselect: ((Event) -> JsAny?)?
    override var onshow: ((Event) -> JsAny?)?
    override var onstalled: ((Event) -> JsAny?)?
    override var onsubmit: ((Event) -> JsAny?)?
    override var onsuspend: ((Event) -> JsAny?)?
    override var ontimeupdate: ((Event) -> JsAny?)?
    override var ontoggle: ((Event) -> JsAny?)?
    override var onvolumechange: ((Event) -> JsAny?)?
    override var onwaiting: ((Event) -> JsAny?)?
    override var ongotpointercapture: ((PointerEvent) -> JsAny?)?
    override var onlostpointercapture: ((PointerEvent) -> JsAny?)?
    override var onpointerdown: ((PointerEvent) -> JsAny?)?
    override var onpointermove: ((PointerEvent) -> JsAny?)?
    override var onpointerup: ((PointerEvent) -> JsAny?)?
    override var onpointercancel: ((PointerEvent) -> JsAny?)?
    override var onpointerover: ((PointerEvent) -> JsAny?)?
    override var onpointerout: ((PointerEvent) -> JsAny?)?
    override var onpointerenter: ((PointerEvent) -> JsAny?)?
    override var onpointerleave: ((PointerEvent) -> JsAny?)?
    override var oncopy: ((ClipboardEvent) -> JsAny?)?
    override var oncut: ((ClipboardEvent) -> JsAny?)?
    override var onpaste: ((ClipboardEvent) -> JsAny?)?
    override var contentEditable: String
    override konst isContentEditable: Boolean
    override konst style: CSSStyleDeclaration
    override konst children: HTMLCollection
    override konst firstElementChild: Element?
    override konst lastElementChild: Element?
    override konst childElementCount: Int
    override konst previousElementSibling: Element?
    override konst nextElementSibling: Element?
    override konst assignedSlot: HTMLSlotElement?
    override fun prepend(vararg nodes: JsAny?)
    override fun append(vararg nodes: JsAny?)
    override fun querySelector(selectors: String): Element?
    override fun querySelectorAll(selectors: String): NodeList
    override fun before(vararg nodes: JsAny?)
    override fun after(vararg nodes: JsAny?)
    override fun replaceWith(vararg nodes: JsAny?)
    override fun remove()
    override fun getBoxQuads(options: BoxQuadOptions /* = definedExternally */): JsArray<DOMQuad>
    override fun convertQuadFromNode(quad: JsAny?, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertRectFromNode(rect: DOMRectReadOnly, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMQuad
    override fun convertPointFromNode(point: DOMPointInit, from: JsAny?, options: ConvertCoordinateOptions /* = definedExternally */): DOMPoint

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

public external interface UnionElementOrHTMLCollection

public external interface UnionElementOrRadioNodeList

public external interface UnionHTMLOptGroupElementOrHTMLOptionElement

public external interface UnionAudioTrackOrTextTrackOrVideoTrack

public external interface UnionElementOrMouseEvent

public external interface UnionMessagePortOrWindowProxy

public external interface MediaProvider

public external interface RenderingContext

public external interface HTMLOrSVGImageElement : CanvasImageSource

public external interface CanvasImageSource : ImageBitmapSource

public external interface ImageBitmapSource

public external interface HTMLOrSVGScriptElement

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface DocumentReadyState : JsAny {
    companion object
}

public inline konst DocumentReadyState.Companion.LOADING: DocumentReadyState get() = "loading".toJsString().unsafeCast<DocumentReadyState>()

public inline konst DocumentReadyState.Companion.INTERACTIVE: DocumentReadyState get() = "interactive".toJsString().unsafeCast<DocumentReadyState>()

public inline konst DocumentReadyState.Companion.COMPLETE: DocumentReadyState get() = "complete".toJsString().unsafeCast<DocumentReadyState>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface CanPlayTypeResult : JsAny {
    companion object
}

public inline konst CanPlayTypeResult.Companion.EMPTY: CanPlayTypeResult get() = "".toJsString().unsafeCast<CanPlayTypeResult>()

public inline konst CanPlayTypeResult.Companion.MAYBE: CanPlayTypeResult get() = "maybe".toJsString().unsafeCast<CanPlayTypeResult>()

public inline konst CanPlayTypeResult.Companion.PROBABLY: CanPlayTypeResult get() = "probably".toJsString().unsafeCast<CanPlayTypeResult>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface TextTrackMode : JsAny {
    companion object
}

public inline konst TextTrackMode.Companion.DISABLED: TextTrackMode get() = "disabled".toJsString().unsafeCast<TextTrackMode>()

public inline konst TextTrackMode.Companion.HIDDEN: TextTrackMode get() = "hidden".toJsString().unsafeCast<TextTrackMode>()

public inline konst TextTrackMode.Companion.SHOWING: TextTrackMode get() = "showing".toJsString().unsafeCast<TextTrackMode>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface TextTrackKind : JsAny {
    companion object
}

public inline konst TextTrackKind.Companion.SUBTITLES: TextTrackKind get() = "subtitles".toJsString().unsafeCast<TextTrackKind>()

public inline konst TextTrackKind.Companion.CAPTIONS: TextTrackKind get() = "captions".toJsString().unsafeCast<TextTrackKind>()

public inline konst TextTrackKind.Companion.DESCRIPTIONS: TextTrackKind get() = "descriptions".toJsString().unsafeCast<TextTrackKind>()

public inline konst TextTrackKind.Companion.CHAPTERS: TextTrackKind get() = "chapters".toJsString().unsafeCast<TextTrackKind>()

public inline konst TextTrackKind.Companion.METADATA: TextTrackKind get() = "metadata".toJsString().unsafeCast<TextTrackKind>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface SelectionMode : JsAny {
    companion object
}

public inline konst SelectionMode.Companion.SELECT: SelectionMode get() = "select".toJsString().unsafeCast<SelectionMode>()

public inline konst SelectionMode.Companion.START: SelectionMode get() = "start".toJsString().unsafeCast<SelectionMode>()

public inline konst SelectionMode.Companion.END: SelectionMode get() = "end".toJsString().unsafeCast<SelectionMode>()

public inline konst SelectionMode.Companion.PRESERVE: SelectionMode get() = "preserve".toJsString().unsafeCast<SelectionMode>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface CanvasFillRule : JsAny {
    companion object
}

public inline konst CanvasFillRule.Companion.NONZERO: CanvasFillRule get() = "nonzero".toJsString().unsafeCast<CanvasFillRule>()

public inline konst CanvasFillRule.Companion.EVENODD: CanvasFillRule get() = "evenodd".toJsString().unsafeCast<CanvasFillRule>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface ImageSmoothingQuality : JsAny {
    companion object
}

public inline konst ImageSmoothingQuality.Companion.LOW: ImageSmoothingQuality get() = "low".toJsString().unsafeCast<ImageSmoothingQuality>()

public inline konst ImageSmoothingQuality.Companion.MEDIUM: ImageSmoothingQuality get() = "medium".toJsString().unsafeCast<ImageSmoothingQuality>()

public inline konst ImageSmoothingQuality.Companion.HIGH: ImageSmoothingQuality get() = "high".toJsString().unsafeCast<ImageSmoothingQuality>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface CanvasLineCap : JsAny {
    companion object
}

public inline konst CanvasLineCap.Companion.BUTT: CanvasLineCap get() = "butt".toJsString().unsafeCast<CanvasLineCap>()

public inline konst CanvasLineCap.Companion.ROUND: CanvasLineCap get() = "round".toJsString().unsafeCast<CanvasLineCap>()

public inline konst CanvasLineCap.Companion.SQUARE: CanvasLineCap get() = "square".toJsString().unsafeCast<CanvasLineCap>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface CanvasLineJoin : JsAny {
    companion object
}

public inline konst CanvasLineJoin.Companion.ROUND: CanvasLineJoin get() = "round".toJsString().unsafeCast<CanvasLineJoin>()

public inline konst CanvasLineJoin.Companion.BEVEL: CanvasLineJoin get() = "bevel".toJsString().unsafeCast<CanvasLineJoin>()

public inline konst CanvasLineJoin.Companion.MITER: CanvasLineJoin get() = "miter".toJsString().unsafeCast<CanvasLineJoin>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface CanvasTextAlign : JsAny {
    companion object
}

public inline konst CanvasTextAlign.Companion.START: CanvasTextAlign get() = "start".toJsString().unsafeCast<CanvasTextAlign>()

public inline konst CanvasTextAlign.Companion.END: CanvasTextAlign get() = "end".toJsString().unsafeCast<CanvasTextAlign>()

public inline konst CanvasTextAlign.Companion.LEFT: CanvasTextAlign get() = "left".toJsString().unsafeCast<CanvasTextAlign>()

public inline konst CanvasTextAlign.Companion.RIGHT: CanvasTextAlign get() = "right".toJsString().unsafeCast<CanvasTextAlign>()

public inline konst CanvasTextAlign.Companion.CENTER: CanvasTextAlign get() = "center".toJsString().unsafeCast<CanvasTextAlign>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface CanvasTextBaseline : JsAny {
    companion object
}

public inline konst CanvasTextBaseline.Companion.TOP: CanvasTextBaseline get() = "top".toJsString().unsafeCast<CanvasTextBaseline>()

public inline konst CanvasTextBaseline.Companion.HANGING: CanvasTextBaseline get() = "hanging".toJsString().unsafeCast<CanvasTextBaseline>()

public inline konst CanvasTextBaseline.Companion.MIDDLE: CanvasTextBaseline get() = "middle".toJsString().unsafeCast<CanvasTextBaseline>()

public inline konst CanvasTextBaseline.Companion.ALPHABETIC: CanvasTextBaseline get() = "alphabetic".toJsString().unsafeCast<CanvasTextBaseline>()

public inline konst CanvasTextBaseline.Companion.IDEOGRAPHIC: CanvasTextBaseline get() = "ideographic".toJsString().unsafeCast<CanvasTextBaseline>()

public inline konst CanvasTextBaseline.Companion.BOTTOM: CanvasTextBaseline get() = "bottom".toJsString().unsafeCast<CanvasTextBaseline>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface CanvasDirection : JsAny {
    companion object
}

public inline konst CanvasDirection.Companion.LTR: CanvasDirection get() = "ltr".toJsString().unsafeCast<CanvasDirection>()

public inline konst CanvasDirection.Companion.RTL: CanvasDirection get() = "rtl".toJsString().unsafeCast<CanvasDirection>()

public inline konst CanvasDirection.Companion.INHERIT: CanvasDirection get() = "inherit".toJsString().unsafeCast<CanvasDirection>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface ScrollRestoration : JsAny {
    companion object
}

public inline konst ScrollRestoration.Companion.AUTO: ScrollRestoration get() = "auto".toJsString().unsafeCast<ScrollRestoration>()

public inline konst ScrollRestoration.Companion.MANUAL: ScrollRestoration get() = "manual".toJsString().unsafeCast<ScrollRestoration>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface ImageOrientation : JsAny {
    companion object
}

public inline konst ImageOrientation.Companion.NONE: ImageOrientation get() = "none".toJsString().unsafeCast<ImageOrientation>()

public inline konst ImageOrientation.Companion.FLIPY: ImageOrientation get() = "flipY".toJsString().unsafeCast<ImageOrientation>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface PremultiplyAlpha : JsAny {
    companion object
}

public inline konst PremultiplyAlpha.Companion.NONE: PremultiplyAlpha get() = "none".toJsString().unsafeCast<PremultiplyAlpha>()

public inline konst PremultiplyAlpha.Companion.PREMULTIPLY: PremultiplyAlpha get() = "premultiply".toJsString().unsafeCast<PremultiplyAlpha>()

public inline konst PremultiplyAlpha.Companion.DEFAULT: PremultiplyAlpha get() = "default".toJsString().unsafeCast<PremultiplyAlpha>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface ColorSpaceConversion : JsAny {
    companion object
}

public inline konst ColorSpaceConversion.Companion.NONE: ColorSpaceConversion get() = "none".toJsString().unsafeCast<ColorSpaceConversion>()

public inline konst ColorSpaceConversion.Companion.DEFAULT: ColorSpaceConversion get() = "default".toJsString().unsafeCast<ColorSpaceConversion>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface ResizeQuality : JsAny {
    companion object
}

public inline konst ResizeQuality.Companion.PIXELATED: ResizeQuality get() = "pixelated".toJsString().unsafeCast<ResizeQuality>()

public inline konst ResizeQuality.Companion.LOW: ResizeQuality get() = "low".toJsString().unsafeCast<ResizeQuality>()

public inline konst ResizeQuality.Companion.MEDIUM: ResizeQuality get() = "medium".toJsString().unsafeCast<ResizeQuality>()

public inline konst ResizeQuality.Companion.HIGH: ResizeQuality get() = "high".toJsString().unsafeCast<ResizeQuality>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface BinaryType : JsAny {
    companion object
}

public inline konst BinaryType.Companion.BLOB: BinaryType get() = "blob".toJsString().unsafeCast<BinaryType>()

public inline konst BinaryType.Companion.ARRAYBUFFER: BinaryType get() = "arraybuffer".toJsString().unsafeCast<BinaryType>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface WorkerType : JsAny {
    companion object
}

public inline konst WorkerType.Companion.CLASSIC: WorkerType get() = "classic".toJsString().unsafeCast<WorkerType>()

public inline konst WorkerType.Companion.MODULE: WorkerType get() = "module".toJsString().unsafeCast<WorkerType>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface ShadowRootMode : JsAny {
    companion object
}

public inline konst ShadowRootMode.Companion.OPEN: ShadowRootMode get() = "open".toJsString().unsafeCast<ShadowRootMode>()

public inline konst ShadowRootMode.Companion.CLOSED: ShadowRootMode get() = "closed".toJsString().unsafeCast<ShadowRootMode>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface ScrollBehavior : JsAny {
    companion object
}

public inline konst ScrollBehavior.Companion.AUTO: ScrollBehavior get() = "auto".toJsString().unsafeCast<ScrollBehavior>()

public inline konst ScrollBehavior.Companion.INSTANT: ScrollBehavior get() = "instant".toJsString().unsafeCast<ScrollBehavior>()

public inline konst ScrollBehavior.Companion.SMOOTH: ScrollBehavior get() = "smooth".toJsString().unsafeCast<ScrollBehavior>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface ScrollLogicalPosition : JsAny {
    companion object
}

public inline konst ScrollLogicalPosition.Companion.START: ScrollLogicalPosition get() = "start".toJsString().unsafeCast<ScrollLogicalPosition>()

public inline konst ScrollLogicalPosition.Companion.CENTER: ScrollLogicalPosition get() = "center".toJsString().unsafeCast<ScrollLogicalPosition>()

public inline konst ScrollLogicalPosition.Companion.END: ScrollLogicalPosition get() = "end".toJsString().unsafeCast<ScrollLogicalPosition>()

public inline konst ScrollLogicalPosition.Companion.NEAREST: ScrollLogicalPosition get() = "nearest".toJsString().unsafeCast<ScrollLogicalPosition>()

/* please, don't implement this interface! */
@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface CSSBoxType : JsAny {
    companion object
}

public inline konst CSSBoxType.Companion.MARGIN: CSSBoxType get() = "margin".toJsString().unsafeCast<CSSBoxType>()

public inline konst CSSBoxType.Companion.BORDER: CSSBoxType get() = "border".toJsString().unsafeCast<CSSBoxType>()

public inline konst CSSBoxType.Companion.PADDING: CSSBoxType get() = "padding".toJsString().unsafeCast<CSSBoxType>()

public inline konst CSSBoxType.Companion.CONTENT: CSSBoxType get() = "content".toJsString().unsafeCast<CSSBoxType>()