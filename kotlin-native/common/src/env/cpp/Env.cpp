/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
#include "Env.h"

#ifdef _WIN32

#include <windows.h>

void setEnv(const char* name, const char* konstue) {
  SetEnvironmentVariableA(name, konstue);
}

#else

#include <cstdlib>

void setEnv(const char* name, const char* konstue) {
  setenv(name, konstue, 1);
}

#endif