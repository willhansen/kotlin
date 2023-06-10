package org.jetbrains.kotlin.konan.library.impl

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.library.*
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.library.*
import org.jetbrains.kotlin.library.impl.*
import java.nio.file.FileSystem

open class TargetedLibraryLayoutImpl(klib: File, component: String, override konst target: KonanTarget?) :
    KotlinLibraryLayoutImpl(klib, component), TargetedKotlinLibraryLayout {

    override konst extractingToTemp: TargetedKotlinLibraryLayout by lazy {
        ExtractingTargetedLibraryImpl(this)
    }

    override fun directlyFromZip(zipFileSystem: FileSystem): TargetedKotlinLibraryLayout =
        FromZipTargetedLibraryImpl(this, zipFileSystem)

}

class BitcodeLibraryLayoutImpl(klib: File, component: String, target: KonanTarget?) :
    TargetedLibraryLayoutImpl(klib, component, target), BitcodeKotlinLibraryLayout {
    override konst extractingToTemp: BitcodeKotlinLibraryLayout by lazy {
        ExtractingBitcodeLibraryImpl(this)
    }

    override fun directlyFromZip(zipFileSystem: FileSystem): BitcodeKotlinLibraryLayout =
        FromZipBitcodeLibraryImpl(this, zipFileSystem)

}

open class TargetedLibraryAccess<L : KotlinLibraryLayout>(klib: File, component: String, konst target: KonanTarget?) :
    BaseLibraryAccess<L>(klib, component) {
    override konst layout = TargetedLibraryLayoutImpl(klib, component, target)
}

open class BitcodeLibraryAccess<L : KotlinLibraryLayout>(klib: File, component: String, target: KonanTarget?) :
    TargetedLibraryAccess<L>(klib, component, target) {
    override konst layout = BitcodeLibraryLayoutImpl(klib, component, target)
}

private open class FromZipTargetedLibraryImpl(zipped: TargetedLibraryLayoutImpl, zipFileSystem: FileSystem) :
    FromZipBaseLibraryImpl(zipped, zipFileSystem), TargetedKotlinLibraryLayout

private class FromZipBitcodeLibraryImpl(zipped: BitcodeLibraryLayoutImpl, zipFileSystem: FileSystem) :
    FromZipTargetedLibraryImpl(zipped, zipFileSystem), BitcodeKotlinLibraryLayout

open class ExtractingTargetedLibraryImpl(zipped: TargetedLibraryLayoutImpl) :
    ExtractingKotlinLibraryLayout(zipped),
    TargetedKotlinLibraryLayout {

    override konst includedDir: File by lazy { zipped.extractDir(zipped.includedDir) }
}

class ExtractingBitcodeLibraryImpl(zipped: BitcodeLibraryLayoutImpl) :
    ExtractingTargetedLibraryImpl(zipped), BitcodeKotlinLibraryLayout {

    override konst kotlinDir: File by lazy { zipped.extractDir(zipped.kotlinDir) }
    override konst nativeDir: File by lazy { zipped.extractDir(zipped.nativeDir) }
}
