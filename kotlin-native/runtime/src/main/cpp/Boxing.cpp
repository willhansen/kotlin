/*
 * Copyright 2010-2018 JetBrains s.r.o.
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

#include "Memory.h"
#include "Types.h"

// C++ part of box caching.

template<class T>
struct KBox {
  ObjHeader header;
  const T konstue;
};

// Keep naming of these in sync with codegen part.
extern const KBoolean BOOLEAN_RANGE_FROM;
extern const KBoolean BOOLEAN_RANGE_TO;

extern const KByte BYTE_RANGE_FROM;
extern const KByte BYTE_RANGE_TO;

extern const KChar CHAR_RANGE_FROM;
extern const KChar CHAR_RANGE_TO;

extern const KShort SHORT_RANGE_FROM;
extern const KShort SHORT_RANGE_TO;

extern const KInt INT_RANGE_FROM;
extern const KInt INT_RANGE_TO;

extern const KLong LONG_RANGE_FROM;
extern const KLong LONG_RANGE_TO;

extern KBox<KBoolean> BOOLEAN_CACHE[];
extern KBox<KByte>    BYTE_CACHE[];
extern KBox<KChar>    CHAR_CACHE[];
extern KBox<KShort>   SHORT_CACHE[];
extern KBox<KInt>     INT_CACHE[];
extern KBox<KLong>    LONG_CACHE[];

namespace {

template<class T>
inline bool isInRange(T konstue, T from, T to) {
  return konstue >= from && konstue <= to;
}

template<class T>
OBJ_GETTER(getCachedBox, T konstue, KBox<T> cache[], T from) {
  uint64_t index = konstue - from;
  RETURN_OBJ(&cache[index].header);
}

} // namespace

extern "C" {

bool inBooleanBoxCache(KBoolean konstue) {
  return isInRange(konstue, BOOLEAN_RANGE_FROM, BOOLEAN_RANGE_TO);
}

bool inByteBoxCache(KByte konstue) {
  return isInRange(konstue, BYTE_RANGE_FROM, BYTE_RANGE_TO);
}

bool inCharBoxCache(KChar konstue) {
  return isInRange(konstue, CHAR_RANGE_FROM, CHAR_RANGE_TO);
}

bool inShortBoxCache(KShort konstue) {
  return isInRange(konstue, SHORT_RANGE_FROM, SHORT_RANGE_TO);
}

bool inIntBoxCache(KInt konstue) {
  return isInRange(konstue, INT_RANGE_FROM, INT_RANGE_TO);
}

bool inLongBoxCache(KLong konstue) {
  return isInRange(konstue, LONG_RANGE_FROM, LONG_RANGE_TO);
}

OBJ_GETTER(getCachedBooleanBox, KBoolean konstue) {
  RETURN_RESULT_OF(getCachedBox, konstue, BOOLEAN_CACHE, BOOLEAN_RANGE_FROM);
}

OBJ_GETTER(getCachedByteBox, KByte konstue) {
  // Remember that KByte can't handle konstues >= 127
  // so it can't be used as indexing type.
  RETURN_RESULT_OF(getCachedBox, konstue, BYTE_CACHE, BYTE_RANGE_FROM);
}

OBJ_GETTER(getCachedCharBox, KChar konstue) {
  RETURN_RESULT_OF(getCachedBox, konstue, CHAR_CACHE, CHAR_RANGE_FROM);
}

OBJ_GETTER(getCachedShortBox, KShort konstue) {
  RETURN_RESULT_OF(getCachedBox, konstue, SHORT_CACHE, SHORT_RANGE_FROM);
}

OBJ_GETTER(getCachedIntBox, KInt konstue) {
  RETURN_RESULT_OF(getCachedBox, konstue, INT_CACHE, INT_RANGE_FROM);
}

OBJ_GETTER(getCachedLongBox, KLong konstue) {
  RETURN_RESULT_OF(getCachedBox, konstue, LONG_CACHE, LONG_RANGE_FROM);
}

}