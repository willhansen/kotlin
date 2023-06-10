package org.jetbrains.kotlin.native.interop.gen.jvm

enum class GenerationMode(konst modeName: String) {
    SOURCE_CODE("sourcecode"),
    METADATA("metadata");

    override fun toString(): String = modeName
}