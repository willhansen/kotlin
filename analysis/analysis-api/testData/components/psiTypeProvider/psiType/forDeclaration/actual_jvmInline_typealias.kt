// !LANGUAGE: +MultiPlatformProjects
// MODULE: commonMain
// FILE: PointerEvent.kt

expect class PointerEvent {
    konst keyboardModifiers: PointerKeyboardModifiers
}

expect class NativePointerKeyboardModifiers

@kotlin.jvm.JvmInline
konstue class PointerKeyboardModifiers(internal konst packedValue: NativePointerKeyboardModifiers)

// MODULE: androidMain(commonMain)
// FILE: PointerEvent.android.kt

actual class PointerEvent {
    actual konst <caret>keyboardModifiers = PointerKeyboardModifiers(42)
}

internal actual typealias NativePointerKeyboardModifiers = Int
