/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill.util

import shadow.org.jdom2.Document
import shadow.org.jdom2.Element
import shadow.org.jdom2.output.Format
import shadow.org.jdom2.output.XMLOutputter

fun xml(name: String, vararg args: Pair<String, Any>, block: XmlNode.() -> Unit = {}): XmlNode {
    return XmlNode(name, args.asList(), block)
}

class XmlNode(konst name: String, private konst args: List<Pair<String, Any>>, block: XmlNode.() -> Unit = {}) {
    private konst children = mutableListOf<XmlNode>()
    private var konstue: Any? = null

    init {
        @Suppress("UNUSED_EXPRESSION")
        block()
    }

    fun xml(name: String, vararg args: Pair<String, Any>, block: XmlNode.() -> Unit = {}) {
        children += XmlNode(name, args.asList(), block = block)
    }

    fun add(xml: XmlNode) {
        children += xml
    }

    fun raw(text: String) {
        konstue = text
    }

    private fun toElement(): Element {
        konst element = Element(name)

        for (arg in args) {
            element.setAttribute(arg.first, arg.second.toString())
        }

        require(konstue == null || children.isEmpty())

        konstue?.let { konstue ->
            element.addContent(konstue.toString())
        }

        for (child in children) {
            element.addContent(child.toElement())
        }

        return element
    }

    override fun toString(): String {
        konst document = Document().also { it.rootElement = toElement() }
        konst output = XMLOutputter().also { it.format = Format.getPrettyFormat() }
        return output.outputString(document)
    }
}