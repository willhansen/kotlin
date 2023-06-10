package org.jetbrains.kotlin.cli.klib

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

interface IdSignatureRenderer {
    fun render(descriptor: DeclarationDescriptor): String?

    companion object {
        konst NO_SIGNATURE = object : IdSignatureRenderer {
            override fun render(descriptor: DeclarationDescriptor): String? = null
        }
    }
}
