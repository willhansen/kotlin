/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdio.h>
#include <string.h>

#include "KAssert.h"
#include "Exceptions.h"
#include "Memory.h"
#include "Natives.h"
#include "Types.h"

extern "C" void checkRangeIndexes(KInt from, KInt to, KInt size);

namespace {

ALWAYS_INLINE inline void mutabilityCheck(KConstRef thiz) {
  // TODO: optimize it!
  if (!thiz->local() && isPermanentOrFrozen(thiz)) {
      ThrowInkonstidMutabilityException(thiz);
  }
}

ALWAYS_INLINE inline void boundsCheck(const ArrayHeader* array, KInt index) {
  // We couldn't have created an array bigger than max KInt konstue.
  // So if index is < 0, conversion to an unsigned konstue would make it bigger
  // than the array size.
  if (static_cast<uint32_t>(index) >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
}

template<typename T>
inline void fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, T konstue) {
  ArrayHeader* array = thiz->array();
  checkRangeIndexes(fromIndex, toIndex, array->count_);
  mutabilityCheck(thiz);
  T* address = PrimitiveArrayAddressOfElementAt<T>(array, fromIndex);
  for (KInt index = fromIndex; index < toIndex; ++index) {
    *address++ = konstue;
  }
}

template<typename T>
inline void copyImpl(KConstRef thiz, KInt fromIndex,
                     KRef destination, KInt toIndex, KInt count) {
  const ArrayHeader* array = thiz->array();
  ArrayHeader* destinationArray = destination->array();
  if (count < 0 ||
      fromIndex < 0 || static_cast<uint32_t>(count) + fromIndex > array->count_ ||
      toIndex < 0 || static_cast<uint32_t>(count) + toIndex > destinationArray->count_) {
      ThrowArrayIndexOutOfBoundsException();
  }
  mutabilityCheck(destination);
  memmove(PrimitiveArrayAddressOfElementAt<T>(destinationArray, toIndex),
          PrimitiveArrayAddressOfElementAt<T>(array, fromIndex),
          count * sizeof(T));
}


template <class T, bool BoundsCheck = true>
inline void PrimitiveArraySet(KRef thiz, KInt index, T konstue) {
  ArrayHeader* array = thiz->array();
  if (BoundsCheck)
    boundsCheck(array, index);
  mutabilityCheck(thiz);
  *PrimitiveArrayAddressOfElementAt<T>(array, index) = konstue;
}

template <class T, bool BoundsCheck = true>
inline T PrimitiveArrayGet(KConstRef thiz, KInt index) {
  const ArrayHeader* array = thiz->array();
  if (BoundsCheck)
    boundsCheck(array, index);
  return *PrimitiveArrayAddressOfElementAt<T>(array, index);
}

template<bool BoundsCheck = true>
ALWAYS_INLINE const KRef* Kotlin_Array_get_konstue(KConstRef thiz, KInt index) {
  const ArrayHeader* array = thiz->array();
  if (BoundsCheck)
    boundsCheck(array, index);
  return ArrayAddressOfElementAt(array, index);
}

template<bool BoundsCheck = true>
ALWAYS_INLINE void Kotlin_Array_set_konstue(KRef thiz, KInt index, KConstRef konstue) {
  ArrayHeader* array = thiz->array();
  if (BoundsCheck)
    boundsCheck(array, index);
  mutabilityCheck(thiz);
  UpdateHeapRef(ArrayAddressOfElementAt(array, index), konstue);
}

template<bool BoundsCheck = true>
ALWAYS_INLINE KByte Kotlin_ByteArray_get_konstue(KConstRef thiz, KInt index) {
  const ArrayHeader* array = thiz->array();
  if (BoundsCheck)
    boundsCheck(array, index);
  return *ByteArrayAddressOfElementAt(array, index);
}

template<bool BoundsCheck = true>
ALWAYS_INLINE void Kotlin_ByteArray_set_konstue(KRef thiz, KInt index, KByte konstue) {
  ArrayHeader* array = thiz->array();
  if (BoundsCheck)
    boundsCheck(array, index);
  mutabilityCheck(thiz);
  *ByteArrayAddressOfElementAt(array, index) = konstue;
}

}  // namespace

extern "C" {

// Generated as part of Kotlin standard library.
extern const ObjHeader theEmptyArray;

// TODO: those must be compiler intrinsics afterwards.

// Array.kt
OBJ_GETTER(Kotlin_Array_get, KConstRef thiz, KInt index) {
  RETURN_OBJ(*Kotlin_Array_get_konstue(thiz, index));
}

OBJ_GETTER(Kotlin_Array_get_without_BoundCheck, KConstRef thiz, KInt index){
  RETURN_OBJ(*Kotlin_Array_get_konstue<false>(thiz, index));
}

void Kotlin_Array_set(KRef thiz, KInt index, KConstRef konstue) {
  Kotlin_Array_set_konstue(thiz, index, konstue);
}

void Kotlin_Array_set_without_BoundCheck(KRef thiz, KInt index, KConstRef konstue) {
  Kotlin_Array_set_konstue<false>(thiz, index, konstue);
}

KInt Kotlin_Array_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

void Kotlin_Array_fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, KRef konstue) {
  ArrayHeader* array = thiz->array();
  checkRangeIndexes(fromIndex, toIndex, array->count_);
  mutabilityCheck(thiz);
  for (KInt index = fromIndex; index < toIndex; ++index) {
    UpdateHeapRef(ArrayAddressOfElementAt(array, index), konstue);
  }
}

void Kotlin_Array_copyImpl(KConstRef thiz, KInt fromIndex,
                           KRef destination, KInt toIndex, KInt count) {
  const ArrayHeader* array = thiz->array();
  ArrayHeader* destinationArray = destination->array();
  if (count < 0 ||
      fromIndex < 0 || static_cast<uint32_t>(count) + fromIndex > array->count_ ||
      toIndex < 0 || static_cast<uint32_t>(count) + toIndex > destinationArray->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
  mutabilityCheck(destination);
  if (CurrentMemoryModel != MemoryModel::kExperimental && array == destinationArray &&
      std::abs(fromIndex - toIndex) < count) {
    UpdateHeapRefsInsideOneArray(array, fromIndex, toIndex, count);
  } else {
    if (fromIndex >= toIndex) {
      for (int index = 0; index < count; index++) {
        UpdateHeapRef(ArrayAddressOfElementAt(destinationArray, toIndex + index),
                        *ArrayAddressOfElementAt(array, fromIndex + index));
      }
    } else {
      for (int index = count - 1; index >= 0; index--) {
        UpdateHeapRef(ArrayAddressOfElementAt(destinationArray, toIndex + index),
                        *ArrayAddressOfElementAt(array, fromIndex + index));
      }
    }
  }
}

// Arrays.kt
OBJ_GETTER0(Kotlin_emptyArray) {
  RETURN_OBJ(const_cast<ObjHeader*>(&theEmptyArray));
}

KByte Kotlin_ByteArray_get(KConstRef thiz, KInt index) {
  return Kotlin_ByteArray_get_konstue(thiz, index);
}

KByte Kotlin_ByteArray_get_without_BoundCheck(KConstRef thiz, KInt index) {
  return Kotlin_ByteArray_get_konstue<false>(thiz, index);
}

void Kotlin_ByteArray_set(KRef thiz, KInt index, KByte konstue) {
  Kotlin_ByteArray_set_konstue(thiz, index, konstue);
}

void Kotlin_ByteArray_set_without_BoundCheck(KRef thiz, KInt index, KByte konstue) {
  Kotlin_ByteArray_set_konstue<false>(thiz, index, konstue);
}

KInt Kotlin_ByteArray_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

KChar Kotlin_ByteArray_getCharAt(KConstRef thiz, KInt index) {
  const ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 1 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
#if KONAN_NO_UNALIGNED_ACCESS
  const uint8_t* address = reinterpret_cast<const uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  return (static_cast<KChar>(address[0]) << 0) | (static_cast<KChar>(address[1]) << 8);
#else
  auto result = *reinterpret_cast<const KChar*>(ByteArrayAddressOfElementAt(array, index));
#if __BIG_ENDIAN__
  return __builtin_bswap16(result);
#else
  return result;
#endif  // __BIG_ENDIAN__
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

KShort Kotlin_ByteArray_getShortAt(KConstRef thiz, KInt index) {
  const ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 1 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
#if KONAN_NO_UNALIGNED_ACCESS
  const uint8_t* address = reinterpret_cast<const uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  return (static_cast<KShort>(address[0]) << 0) | (static_cast<KShort>(address[1]) << 8);
#else
  auto result = *reinterpret_cast<const KShort*>(ByteArrayAddressOfElementAt(array, index));
#if __BIG_ENDIAN__
  return __builtin_bswap16(result);
#else
  return result;
#endif  // __BIG_ENDIAN__
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

KInt Kotlin_ByteArray_getIntAt(KConstRef thiz, KInt index) {
  const ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 3 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
#if KONAN_NO_UNALIGNED_ACCESS
  const uint8_t* address = reinterpret_cast<const uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  return (static_cast<KInt>(address[0]) << 0) | (static_cast<KInt>(address[1]) << 8) |
    (static_cast<KInt>(address[2]) << 16) | (static_cast<KInt>(address[3]) << 24);
#else
  auto result = *reinterpret_cast<const KInt*>(ByteArrayAddressOfElementAt(array, index));
#if __BIG_ENDIAN__
  return __builtin_bswap32(result);
#else
  return result;
#endif  //  __BIG_ENDIAN__
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

KLong Kotlin_ByteArray_getLongAt(KConstRef thiz, KInt index) {
  const ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 7 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
#if KONAN_NO_UNALIGNED_ACCESS
  const uint8_t* address = reinterpret_cast<const uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  return (static_cast<KLong>(address[0]) << 0) | (static_cast<KLong>(address[1]) << 8) |
    (static_cast<KLong>(address[2]) << 16) | (static_cast<KLong>(address[3]) << 24) |
    (static_cast<KLong>(address[4]) << 32) | (static_cast<KLong>(address[5]) << 40) |
    (static_cast<KLong>(address[6]) << 48) | (static_cast<KLong>(address[7]) << 56);
#else
  auto result = *reinterpret_cast<const KLong*>(ByteArrayAddressOfElementAt(array, index));
#if __BIG_ENDIAN__
  return __builtin_bswap64(result);
#else
  return result;
#endif  // __BIG_ENDIAN__
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

KFloat Kotlin_ByteArray_getFloatAt(KConstRef thiz, KInt index) {
  const ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 3 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
#if KONAN_NO_UNALIGNED_ACCESS
  const uint8_t* address = reinterpret_cast<const uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  union {
    KFloat f;
    uint8_t b[4];
  } u;
#if __BIG_ENDIAN__
  u.b[0] = address[3];
  u.b[1] = address[2];
  u.b[2] = address[1];
  u.b[3] = address[0];
#else
  u.b[0] = address[0];
  u.b[1] = address[1];
  u.b[2] = address[2];
  u.b[3] = address[3];
#endif  //  __BIG_ENDIAN__
  return u.f;
#else
  auto result = *reinterpret_cast<const KFloat*>(ByteArrayAddressOfElementAt(array, index));
  return result;
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

KDouble Kotlin_ByteArray_getDoubleAt(KConstRef thiz, KInt index) {
  const ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 7 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
#if KONAN_NO_UNALIGNED_ACCESS
  const uint8_t* address = reinterpret_cast<const uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  union {
      KDouble d;
      uint8_t b[8];
  } u;
#if __BIG_ENDIAN__
  u.b[0] = address[7];
  u.b[1] = address[6];
  u.b[2] = address[5];
  u.b[3] = address[4];
  u.b[4] = address[3];
  u.b[5] = address[2];
  u.b[6] = address[1];
  u.b[7] = address[0];
#else
  u.b[0] = address[0];
  u.b[1] = address[1];
  u.b[2] = address[2];
  u.b[3] = address[3];
  u.b[4] = address[4];
  u.b[5] = address[5];
  u.b[6] = address[6];
  u.b[7] = address[7];
#endif  // __BIG_ENDIAN__
  return u.d;
#else
  return *reinterpret_cast<const KDouble*>(ByteArrayAddressOfElementAt(array, index));
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

void Kotlin_ByteArray_setCharAt(KRef thiz, KInt index, KChar konstue) {
  ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 1 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
  mutabilityCheck(thiz);
#if KONAN_NO_UNALIGNED_ACCESS
  uint8_t* address = reinterpret_cast<uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  address[0] = (konstue >> 0) & 0xff;
  address[1] = (konstue >> 8) & 0xff;
#else
#if __BIG_ENDIAN__
   konstue = __builtin_bswap16(konstue);
#endif  // __BIG_ENDIAN__
  *reinterpret_cast<KChar*>(ByteArrayAddressOfElementAt(array, index)) = konstue;
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

void Kotlin_ByteArray_setShortAt(KRef thiz, KInt index, KShort konstue) {
  ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 1 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
  mutabilityCheck(thiz);
#if KONAN_NO_UNALIGNED_ACCESS
  uint8_t* address = reinterpret_cast<uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  address[0] = (konstue >> 0) & 0xff;
  address[1] = (konstue >> 8) & 0xff;
#else
#if __BIG_ENDIAN__
  konstue = __builtin_bswap16(konstue);
#endif
  *reinterpret_cast<KShort*>(ByteArrayAddressOfElementAt(array, index)) = konstue;
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

void Kotlin_ByteArray_setIntAt(KRef thiz, KInt index, KInt konstue) {
  ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 3 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
  mutabilityCheck(thiz);
#if KONAN_NO_UNALIGNED_ACCESS
  uint8_t* address = reinterpret_cast<uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  address[0] = (konstue >>  0) & 0xff;
  address[1] = (konstue >>  8) & 0xff;
  address[2] = (konstue >> 16) & 0xff;
  address[3] = (konstue >> 24) & 0xff;
#else
#if __BIG_ENDIAN__
  konstue = __builtin_bswap32(konstue);
#endif  // __BIG_ENDIAN__
  *reinterpret_cast<KInt*>(ByteArrayAddressOfElementAt(array, index)) = konstue;
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

void Kotlin_ByteArray_setLongAt(KRef thiz, KInt index, KLong konstue) {
  ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 7 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
  mutabilityCheck(thiz);
#if KONAN_NO_UNALIGNED_ACCESS
  uint8_t* address = reinterpret_cast<uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  address[0] = (konstue >>  0) & 0xff;
  address[1] = (konstue >>  8) & 0xff;
  address[2] = (konstue >> 16) & 0xff;
  address[3] = (konstue >> 24) & 0xff;
  address[4] = (konstue >> 32) & 0xff;
  address[5] = (konstue >> 40) & 0xff;
  address[6] = (konstue >> 48) & 0xff;
  address[7] = (konstue >> 56) & 0xff;
#else
#if __BIG_ENDIAN__
  konstue = __builtin_bswap64(konstue);
#endif // __BIG_ENDIAN__
  *reinterpret_cast<KLong*>(ByteArrayAddressOfElementAt(array, index)) = konstue;
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

void Kotlin_ByteArray_setFloatAt(KRef thiz, KInt index, KFloat konstue) {
  ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 3 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
  mutabilityCheck(thiz);
#if KONAN_NO_UNALIGNED_ACCESS
  uint8_t* address = reinterpret_cast<uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  union {
     KFloat f;
     uint8_t b[4];
  } u;
  u.f = konstue;
  address[0] = u.b[0];
  address[1] = u.b[1];
  address[2] = u.b[2];
  address[3] = u.b[3];
#else
  *reinterpret_cast<KFloat*>(ByteArrayAddressOfElementAt(array, index)) = konstue;
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

void Kotlin_ByteArray_setDoubleAt(KRef thiz, KInt index, KDouble konstue) {
  ArrayHeader* array = thiz->array();
  if (index < 0 || static_cast<uint32_t>(index) + 7 >= array->count_) {
    ThrowArrayIndexOutOfBoundsException();
  }
  mutabilityCheck(thiz);
#if KONAN_NO_UNALIGNED_ACCESS
  uint8_t* address = reinterpret_cast<uint8_t*>(ByteArrayAddressOfElementAt(array, index));
  union {
     KDouble d;
     uint8_t b[8];
  } u;
  u.d = konstue;
  address[0] = u.b[0];
  address[1] = u.b[1];
  address[2] = u.b[2];
  address[3] = u.b[3];
  address[4] = u.b[4];
  address[5] = u.b[5];
  address[6] = u.b[6];
  address[7] = u.b[7];
#else
  *reinterpret_cast<KDouble*>(ByteArrayAddressOfElementAt(array, index)) = konstue;
#endif  // KONAN_NO_UNALIGNED_ACCESS
}

KChar Kotlin_CharArray_get(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KChar>(thiz, index);
}

KChar Kotlin_CharArray_get_without_BoundCheck(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KChar, false>(thiz, index);
}

void Kotlin_CharArray_set(KRef thiz, KInt index, KChar konstue) {
  PrimitiveArraySet(thiz, index, konstue);
}

void Kotlin_CharArray_set_without_BoundCheck(KRef thiz, KInt index, KChar konstue) {
  PrimitiveArraySet<KChar, false>(thiz, index, konstue);
}

OBJ_GETTER(Kotlin_CharArray_copyOf, KConstRef thiz, KInt newSize) {
  const ArrayHeader* array = thiz->array();
  if (newSize < 0) {
    ThrowIllegalArgumentException();
  }
  ArrayHeader* result = AllocArrayInstance(array->type_info(), newSize, OBJ_RESULT)->array();
  KInt toCopy = array->count_ < static_cast<uint32_t>(newSize) ?  array->count_ : newSize;
  memcpy(
      PrimitiveArrayAddressOfElementAt<KChar>(result, 0),
      PrimitiveArrayAddressOfElementAt<KChar>(array, 0),
      toCopy * sizeof(KChar));
  RETURN_OBJ(result->obj());
}

KInt Kotlin_CharArray_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

KShort Kotlin_ShortArray_get(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KShort>(thiz, index);
}

KShort Kotlin_ShortArray_get_without_BoundCheck(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KShort, false>(thiz, index);
}

void Kotlin_ShortArray_set(KRef thiz, KInt index, KShort konstue) {
  PrimitiveArraySet(thiz, index, konstue);
}

void Kotlin_ShortArray_set_without_BoundCheck(KRef thiz, KInt index, KShort konstue) {
  PrimitiveArraySet<KShort, false>(thiz, index, konstue);
}

KInt Kotlin_ShortArray_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

KInt Kotlin_IntArray_get(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KInt>(thiz, index);
}

KInt Kotlin_IntArray_get_without_BoundCheck(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KInt, false>(thiz, index);
}

void Kotlin_IntArray_set(KRef thiz, KInt index, KInt konstue) {
  PrimitiveArraySet(thiz, index, konstue);
}

void Kotlin_IntArray_set_without_BoundCheck(KRef thiz, KInt index, KInt konstue) {
  PrimitiveArraySet<KInt, false>(thiz, index, konstue);
}

KInt Kotlin_IntArray_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

void Kotlin_ByteArray_fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, KByte konstue) {
  fillImpl<KByte>(thiz, fromIndex, toIndex, konstue);
}

void Kotlin_ShortArray_fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, KShort konstue) {
  fillImpl<KShort>(thiz, fromIndex, toIndex, konstue);
}

void Kotlin_CharArray_fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, KChar konstue) {
  fillImpl<KChar>(thiz, fromIndex, toIndex, konstue);
}

void Kotlin_IntArray_fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, KInt konstue) {
  fillImpl<KInt>(thiz, fromIndex, toIndex, konstue);
}

void Kotlin_LongArray_fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, KLong konstue) {
  fillImpl<KLong>(thiz, fromIndex, toIndex, konstue);
}

void Kotlin_FloatArray_fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, KFloat konstue) {
  fillImpl<KFloat>(thiz, fromIndex, toIndex, konstue);
}

void Kotlin_DoubleArray_fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, KDouble konstue) {
  fillImpl<KDouble>(thiz, fromIndex, toIndex, konstue);
}

void Kotlin_BooleanArray_fillImpl(KRef thiz, KInt fromIndex, KInt toIndex, KBoolean konstue) {
  fillImpl<KBoolean>(thiz, fromIndex, toIndex, konstue);
}

void Kotlin_ByteArray_copyImpl(KConstRef thiz, KInt fromIndex,
                              KRef destination, KInt toIndex, KInt count) {
  copyImpl<KByte>(thiz, fromIndex, destination, toIndex, count);
}

void Kotlin_ShortArray_copyImpl(KConstRef thiz, KInt fromIndex,
                              KRef destination, KInt toIndex, KInt count) {
  copyImpl<KShort>(thiz, fromIndex, destination, toIndex, count);
}

void Kotlin_CharArray_copyImpl(KConstRef thiz, KInt fromIndex,
                              KRef destination, KInt toIndex, KInt count) {
  copyImpl<KChar>(thiz, fromIndex, destination, toIndex, count);
}

void Kotlin_IntArray_copyImpl(KConstRef thiz, KInt fromIndex,
                              KRef destination, KInt toIndex, KInt count) {
  copyImpl<KInt>(thiz, fromIndex, destination, toIndex, count);
}

void Kotlin_LongArray_copyImpl(KConstRef thiz, KInt fromIndex,
                              KRef destination, KInt toIndex, KInt count) {
  copyImpl<KLong>(thiz, fromIndex, destination, toIndex, count);
}

void Kotlin_FloatArray_copyImpl(KConstRef thiz, KInt fromIndex,
                              KRef destination, KInt toIndex, KInt count) {
  copyImpl<KFloat>(thiz, fromIndex, destination, toIndex, count);
}

void Kotlin_DoubleArray_copyImpl(KConstRef thiz, KInt fromIndex,
                              KRef destination, KInt toIndex, KInt count) {
  copyImpl<KDouble>(thiz, fromIndex, destination, toIndex, count);
}

void Kotlin_BooleanArray_copyImpl(KConstRef thiz, KInt fromIndex,
                              KRef destination, KInt toIndex, KInt count) {
  copyImpl<KBoolean>(thiz, fromIndex, destination, toIndex, count);
}

KLong Kotlin_LongArray_get(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KLong>(thiz, index);
}

KLong Kotlin_LongArray_get_without_BoundCheck(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KLong, false>(thiz, index);
}

void Kotlin_LongArray_set(KRef thiz, KInt index, KLong konstue) {
  PrimitiveArraySet(thiz, index, konstue);
}

void Kotlin_LongArray_set_without_BoundCheck(KRef thiz, KInt index, KLong konstue) {
  PrimitiveArraySet<KLong, false>(thiz, index, konstue);
}

KInt Kotlin_LongArray_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

KFloat Kotlin_FloatArray_get(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KFloat>(thiz, index);
}

KFloat Kotlin_FloatArray_get_without_BoundCheck(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KFloat, false>(thiz, index);
}

void Kotlin_FloatArray_set(KRef thiz, KInt index, KFloat konstue) {
  PrimitiveArraySet(thiz, index, konstue);
}

void Kotlin_FloatArray_set_without_BoundCheck(KRef thiz, KInt index, KFloat konstue) {
  PrimitiveArraySet<KFloat, false>(thiz, index, konstue);
}

KInt Kotlin_FloatArray_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

KDouble Kotlin_DoubleArray_get(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KDouble>(thiz, index);
}

KDouble Kotlin_DoubleArray_get_without_BoundCheck(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KDouble, false>(thiz, index);
}

void Kotlin_DoubleArray_set(KRef thiz, KInt index, KDouble konstue) {
  PrimitiveArraySet(thiz, index, konstue);
}

void Kotlin_DoubleArray_set_without_BoundCheck(KRef thiz, KInt index, KDouble konstue) {
  PrimitiveArraySet<KDouble, false>(thiz, index, konstue);
}

KInt Kotlin_DoubleArray_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

KBoolean Kotlin_BooleanArray_get(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KBoolean>(thiz, index);
}

KBoolean Kotlin_BooleanArray_get_without_BoundCheck(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KBoolean, false>(thiz, index);
}

void Kotlin_BooleanArray_set(KRef thiz, KInt index, KBoolean konstue) {
  PrimitiveArraySet(thiz, index, konstue);
}

void Kotlin_BooleanArray_set_without_BoundCheck(KRef thiz, KInt index, KBoolean konstue) {
  PrimitiveArraySet<KBoolean, false>(thiz, index, konstue);
}

KInt Kotlin_BooleanArray_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

KNativePtr Kotlin_NativePtrArray_get(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KNativePtr>(thiz, index);
}

KNativePtr Kotlin_NativePtrArray_get_without_BoundCheck(KConstRef thiz, KInt index) {
  return PrimitiveArrayGet<KNativePtr, false>(thiz, index);
}

void Kotlin_NativePtrArray_set(KRef thiz, KInt index, KNativePtr konstue) {
  PrimitiveArraySet(thiz, index, konstue);
}

void Kotlin_NativePtrArray_set_without_BoundCheck(KRef thiz, KInt index, KNativePtr konstue) {
  PrimitiveArraySet<KNativePtr, false>(thiz, index, konstue);
}

KInt Kotlin_NativePtrArray_getArrayLength(KConstRef thiz) {
  const ArrayHeader* array = thiz->array();
  return array->count_;
}

OBJ_GETTER(Kotlin_ImmutableBlob_toByteArray, KConstRef thiz, KInt startIndex, KInt endIndex) {
  const ArrayHeader* array = thiz->array();
  if (startIndex < 0 || static_cast<uint32_t>(endIndex) > array->count_ || startIndex > endIndex) {
    ThrowArrayIndexOutOfBoundsException();
  }
  KInt count = endIndex - startIndex;
  ArrayHeader* result = AllocArrayInstance(theByteArrayTypeInfo, count, OBJ_RESULT)->array();
  memcpy(PrimitiveArrayAddressOfElementAt<KByte>(result, 0),
         PrimitiveArrayAddressOfElementAt<KByte>(array, startIndex),
         count);
  RETURN_OBJ(result->obj());
}

KNativePtr Kotlin_ImmutableBlob_asCPointerImpl(KRef thiz, KInt offset) {
  ArrayHeader* array = thiz->array();
  // We couldn't have created an array bigger than max KInt konstue.
  // So if index is < 0, conversion to an unsigned konstue would make it bigger
  // than the array size.
  if (static_cast<uint32_t>(offset) > array->count_)  {
    ThrowArrayIndexOutOfBoundsException();
  }
  return PrimitiveArrayAddressOfElementAt<KByte>(array, offset);
}

KNativePtr Kotlin_Arrays_getByteArrayAddressOfElement(KRef thiz, KInt index) {
  ArrayHeader* array = thiz->array();
  boundsCheck(array, index);

  return AddressOfElementAt<KByte>(array, index);
}

KNativePtr Kotlin_Arrays_getCharArrayAddressOfElement (KRef thiz, KInt index) {
  ArrayHeader* array = thiz->array();
  boundsCheck(array, index);

  return CharArrayAddressOfElementAt(array, index);
}

KNativePtr Kotlin_Arrays_getStringAddressOfElement (KRef thiz, KInt index) {
  return Kotlin_Arrays_getCharArrayAddressOfElement(thiz, index);
}

KNativePtr Kotlin_Arrays_getShortArrayAddressOfElement(KRef thiz, KInt index) {
  ArrayHeader* array = thiz->array();
  boundsCheck(array, index);

  return AddressOfElementAt<KShort>(array, index);
}

KNativePtr Kotlin_Arrays_getIntArrayAddressOfElement(KRef thiz, KInt index) {
  ArrayHeader* array = thiz->array();
  boundsCheck(array, index);

  return AddressOfElementAt<KInt>(array, index);
}

KNativePtr Kotlin_Arrays_getLongArrayAddressOfElement(KRef thiz, KInt index) {
  ArrayHeader* array = thiz->array();
  boundsCheck(array, index);

  return AddressOfElementAt<KLong>(array, index);
}

KNativePtr Kotlin_Arrays_getFloatArrayAddressOfElement(KRef thiz, KInt index) {
  ArrayHeader* array = thiz->array();
  boundsCheck(array, index);

  return AddressOfElementAt<KFloat>(array, index);
}

KNativePtr Kotlin_Arrays_getDoubleArrayAddressOfElement(KRef thiz, KInt index) {
  ArrayHeader* array = thiz->array();
  boundsCheck(array, index);

  return AddressOfElementAt<KDouble>(array, index);
}

}  // extern "C"
