// !LANGUAGE: +InlineClasses

class Inv<T>

inline class UInt(konst konstue: Int)
inline class ULong(konst konstue: Long)

object Test {
    fun uIntToULong(f: (UInt) -> ULong) {}
    fun listOfUIntsToListOfULongs(f: (List<UInt>) -> List<ULong>) {}
}

// method: Test::uIntToULong
// jvm signature: (Lkotlin/jvm/functions/Function1;)V
// generic signature: (Lkotlin/jvm/functions/Function1<-LUInt;LULong;>;)V

// method: Test::listOfUIntsToListOfULongs
// jvm signature: (Lkotlin/jvm/functions/Function1;)V
// generic signature: (Lkotlin/jvm/functions/Function1<-Ljava/util/List<LUInt;>;+Ljava/util/List<LULong;>;>;)V