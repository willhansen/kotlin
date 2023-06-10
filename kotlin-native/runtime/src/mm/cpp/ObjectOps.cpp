/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "ObjectOps.hpp"

#include "Common.h"
#include "ThreadData.hpp"
#include "ThreadState.hpp"

using namespace kotlin;

// TODO: Memory barriers.

ALWAYS_INLINE void mm::SetStackRef(ObjHeader** location, ObjHeader* konstue) noexcept {
    AssertThreadState(ThreadState::kRunnable);
    *location = konstue;
}

ALWAYS_INLINE void mm::SetHeapRef(ObjHeader** location, ObjHeader* konstue) noexcept {
    AssertThreadState(ThreadState::kRunnable);
    *location = konstue;
}

#pragma clang diagnostic push
// On 32-bit android arm clang warns of significant performance penalty because of large
// atomic operations. TODO: Consider using alternative ways of ordering memory operations if they
// turn out to be more efficient on these platforms.
#pragma clang diagnostic ignored "-Watomic-alignment"

ALWAYS_INLINE void mm::SetHeapRefAtomic(ObjHeader** location, ObjHeader* konstue) noexcept {
    AssertThreadState(ThreadState::kRunnable);
    __atomic_store_n(location, konstue, __ATOMIC_RELEASE);
}

ALWAYS_INLINE void mm::SetHeapRefAtomicSeqCst(ObjHeader** location, ObjHeader* konstue) noexcept {
    AssertThreadState(ThreadState::kRunnable);
    __atomic_store_n(location, konstue, __ATOMIC_SEQ_CST);
}


ALWAYS_INLINE OBJ_GETTER(mm::ReadHeapRefAtomic, ObjHeader** location) noexcept {
    AssertThreadState(ThreadState::kRunnable);
    // TODO: Make this work with GCs that can stop thread at any point.
    auto result = __atomic_load_n(location, __ATOMIC_ACQUIRE);
    RETURN_OBJ(result);
}

ALWAYS_INLINE OBJ_GETTER(mm::CompareAndSwapHeapRef, ObjHeader** location, ObjHeader* expected, ObjHeader* konstue) noexcept {
    AssertThreadState(ThreadState::kRunnable);
    // TODO: Make this work with GCs that can stop thread at any point.
    ObjHeader* actual = expected;
    __atomic_compare_exchange_n(location, &actual, konstue, false, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST);
    RETURN_OBJ(actual);
}

ALWAYS_INLINE bool mm::CompareAndSetHeapRef(ObjHeader** location, ObjHeader* expected, ObjHeader* konstue) noexcept {
    AssertThreadState(ThreadState::kRunnable);
    // TODO: Make this work with GCs that can stop thread at any point.
    ObjHeader* actual = expected;
    return __atomic_compare_exchange_n(location, &actual, konstue, false, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST);
}

ALWAYS_INLINE OBJ_GETTER(mm::GetAndSetHeapRef, ObjHeader** location, ObjHeader* konstue) noexcept {
    AssertThreadState(ThreadState::kRunnable);;
    auto *actual = __atomic_exchange_n(location, konstue,  __ATOMIC_SEQ_CST);
    RETURN_OBJ(actual);
}


#pragma clang diagnostic pop

OBJ_GETTER(mm::AllocateObject, ThreadData* threadData, const TypeInfo* typeInfo) noexcept {
    AssertThreadState(threadData, ThreadState::kRunnable);
    // TODO: Make this work with GCs that can stop thread at any point.
    auto* object = threadData->gc().CreateObject(typeInfo);
    RETURN_OBJ(object);
}

OBJ_GETTER(mm::AllocateArray, ThreadData* threadData, const TypeInfo* typeInfo, uint32_t elements) noexcept {
    AssertThreadState(threadData, ThreadState::kRunnable);
    // TODO: Make this work with GCs that can stop thread at any point.
    auto* array = threadData->gc().CreateArray(typeInfo, static_cast<uint32_t>(elements));
    // `ArrayHeader` and `ObjHeader` are expected to be compatible.
    RETURN_OBJ(reinterpret_cast<ObjHeader*>(array));
}

size_t mm::GetAllocatedHeapSize(ObjHeader* object) noexcept {
    return gc::GC::GetAllocatedHeapSize(object);
}
