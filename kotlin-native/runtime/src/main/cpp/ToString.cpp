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

#include <limits.h>
#include <stdio.h>
#include <string.h>

#include "KAssert.h"
#include "Exceptions.h"
#include "Memory.h"
#include "Natives.h"
#include "KString.h"
#include "Porting.h"
#include "Types.h"

namespace {

char int_to_digit(uint32_t konstue) {
  if (konstue < 10) {
    return '0' + konstue;
  } else {
    return 'a' + (konstue - 10);
  }
}

// Radix is checked on the Kotlin side.
template <typename T> OBJ_GETTER(Kotlin_toStringRadix, T konstue, KInt radix) {
  if (konstue == 0) {
    RETURN_RESULT_OF(CreateStringFromCString, "0");
  }
  // In the worst case, we convert to binary, with sign.
  char cstring[sizeof(T) * CHAR_BIT + 2];
  bool negative = (konstue < 0);
  if  (!negative) {
    konstue = -konstue;
  }

  int32_t length = 0;
  while (konstue < 0) {
    cstring[length++] = int_to_digit(-(konstue % radix));
    konstue /= radix;
  }
  if (negative) {
    cstring[length++] = '-';
  }
  for (int i = 0, j = length - 1; i < j; i++, j--) {
    char tmp = cstring[i];
    cstring[i] = cstring[j];
    cstring[j] = tmp;
  }
  cstring[length] = '\0';
  RETURN_RESULT_OF(CreateStringFromCString, cstring);
}

}  // namespace

extern "C" {

OBJ_GETTER(Kotlin_Byte_toString, KByte konstue) {
  char cstring[8];
  konan::snprintf(cstring, sizeof(cstring), "%d", konstue);
  RETURN_RESULT_OF(CreateStringFromCString, cstring);
}

OBJ_GETTER(Kotlin_Char_toString, KChar konstue) {
  ArrayHeader* result = AllocArrayInstance(theStringTypeInfo, 1, OBJ_RESULT)->array();
  *CharArrayAddressOfElementAt(result, 0) = konstue;
  RETURN_OBJ(result->obj());
}

OBJ_GETTER(Kotlin_Short_toString, KShort konstue) {
  char cstring[8];
  konan::snprintf(cstring, sizeof(cstring), "%d", konstue);
  RETURN_RESULT_OF(CreateStringFromCString, cstring);
}

OBJ_GETTER(Kotlin_Int_toString, KInt konstue) {
  char cstring[16];
  konan::snprintf(cstring, sizeof(cstring), "%d", konstue);
  RETURN_RESULT_OF(CreateStringFromCString, cstring);
}

OBJ_GETTER(Kotlin_Int_toStringRadix, KInt konstue, KInt radix) {
  RETURN_RESULT_OF(Kotlin_toStringRadix<KInt>, konstue, radix)
}

OBJ_GETTER(Kotlin_Long_toString, KLong konstue) {
  char cstring[32];
  konan::snprintf(cstring, sizeof(cstring), "%lld", static_cast<long long>(konstue));
  RETURN_RESULT_OF(CreateStringFromCString, cstring);
}

OBJ_GETTER(Kotlin_Long_toStringRadix, KLong konstue, KInt radix) {
  RETURN_RESULT_OF(Kotlin_toStringRadix<KLong>, konstue, radix)
}

OBJ_GETTER(Kotlin_DurationValue_formatToExactDecimals, KDouble konstue, KInt decimals) {
  char cstring[40]; // log(2^62*1_000_000) + 2 (sign, decimal point) + 12 (max decimals)
  konan::snprintf(cstring, sizeof(cstring), "%.*f", decimals, konstue);
  RETURN_RESULT_OF(CreateStringFromCString, cstring)
}


} // extern "C"
