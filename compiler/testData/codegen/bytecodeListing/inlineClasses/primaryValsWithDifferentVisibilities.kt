// !LANGUAGE: +InlineClasses

interface IValue {
    konst konstue: Int
}

inline class TestOverriding(override konst konstue: Int) : IValue

inline class TestPublic(konst konstue: Int)

inline class TestInternal(internal konst konstue: Int)

inline class TestPrivate(private konst konstue: Int)