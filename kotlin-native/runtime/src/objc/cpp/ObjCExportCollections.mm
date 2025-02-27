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

#import "Memory.h"
#import "MemorySharedRefs.hpp"
#import "Types.h"

#if KONAN_OBJC_INTEROP

#import <objc/runtime.h>

#import <Foundation/NSObject.h>
#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSNull.h>

#import "Exceptions.h"
#import "Runtime.h"
#import "ObjCExport.h"
#import "ObjCExportCollections.h"
#import "ObjCExportPrivate.h"

extern "C" {

// Imports from ObjCExportUtils.kt:

OBJ_GETTER0(Kotlin_NSArrayAsKList_create);
OBJ_GETTER0(Kotlin_NSMutableArrayAsKMutableList_create);
OBJ_GETTER0(Kotlin_NSSetAsKSet_create);
OBJ_GETTER0(Kotlin_NSDictionaryAsKMap_create);

KBoolean Kotlin_Iterator_hasNext(KRef iterator);
OBJ_GETTER(Kotlin_Iterator_next, KRef iterator);
KInt Kotlin_Collection_getSize(KRef collection);
KBoolean Kotlin_Set_contains(KRef set, KRef element);
OBJ_GETTER(Kotlin_Set_getElement, KRef set, KRef element);
OBJ_GETTER(Kotlin_Set_iterator, KRef set);
void Kotlin_MutableCollection_removeObject(KRef collection, KRef element);
void Kotlin_MutableCollection_addObject(KRef list, KRef obj);
OBJ_GETTER(Kotlin_MutableSet_createWithCapacity, KInt capacity);

KInt Kotlin_Map_getSize(KRef map);
KBoolean Kotlin_Map_containsKey(KRef map, KRef key);
OBJ_GETTER(Kotlin_Map_get, KRef map, KRef key);
OBJ_GETTER(Kotlin_Map_keyIterator, KRef map);
OBJ_GETTER(Kotlin_List_get, KRef list, KInt index);

OBJ_GETTER(Kotlin_MutableMap_createWithCapacity, KInt capacity);
void Kotlin_MutableMap_set(KRef map, KRef key, KRef konstue);
void Kotlin_MutableMap_remove(KRef map, KRef key);
void Kotlin_MutableList_addObjectAtIndex(KRef list, KInt index, KRef obj);
void Kotlin_MutableList_removeObjectAtIndex(KRef list, KInt index);
void Kotlin_MutableList_removeLastObject(KRef list);
void Kotlin_MutableList_setObject(KRef list, KInt index, KRef obj);

} // extern "C"

static inline KInt objCCapacityToKotlin(NSUInteger capacity) {
  NSUInteger max = std::numeric_limits<KInt>::max();
  return capacity > max ? max : capacity;
}

static inline KInt objCIndexToKotlinOrThrow(NSUInteger index) {
  if (index > std::numeric_limits<KInt>::max()) {
    ThrowArrayIndexOutOfBoundsException();
  }

  return index;
}

// Note: collections can only be iterated on and converted to Kotlin representation
// when they are either frozen or if they are called on the worker that created them.

@interface NSArray (NSArrayToKotlin)
@end

@implementation NSArray (NSArrayToKotlin)
-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_RESULT_OF(invokeAndAssociate, Kotlin_NSArrayAsKList_create, objc_retain(self));
}

-(void)releaseAsAssociatedObject {
  objc_release(self);
}
@end

@interface NSMutableArray (NSMutableArrayToKotlin)
@end

@implementation NSMutableArray (NSArrayToKotlin)
-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_RESULT_OF(invokeAndAssociate, Kotlin_NSMutableArrayAsKMutableList_create, objc_retain(self));
}

-(void)releaseAsAssociatedObject {
  objc_release(self);
}
@end


@interface NSSet (NSSetToKotlin)
@end

@implementation NSSet (NSSetToKotlin)
-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_RESULT_OF(invokeAndAssociate, Kotlin_NSSetAsKSet_create, objc_retain(self));
}

-(void)releaseAsAssociatedObject {
  objc_release(self);
}

@end

@interface NSDictionary (NSDictionaryToKotlin)
@end

@implementation NSDictionary (NSDictionaryToKotlin)
-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_RESULT_OF(invokeAndAssociate, Kotlin_NSDictionaryAsKMap_create, objc_retain(self));
}

-(void)releaseAsAssociatedObject {
  objc_release(self);
}

@end

@interface KIteratorAsNSEnumerator : NSEnumerator
@end

@implementation KIteratorAsNSEnumerator {
  KRefSharedHolder iteratorHolder;
}

-(void)dealloc {
  iteratorHolder.dispose();
  [super dealloc];
}

+(id)createWithKIterator:(KRef)iterator {
  KIteratorAsNSEnumerator* result = [[[KIteratorAsNSEnumerator alloc] init] autorelease];
  result->iteratorHolder.init(iterator);
  return result;
}

- (id)nextObject {
  kotlin::CalledFromNativeGuard guard;
  KRef iterator = iteratorHolder.ref<ErrorPolicy::kTerminate>();
  if (Kotlin_Iterator_hasNext(iterator)) {
    ObjHolder holder;
    return refToObjCOrNSNull(Kotlin_Iterator_next(iterator, holder.slot()));
  } else {
    return nullptr;
  }
}
@end

@interface KListAsNSArray : NSArray
@end

@implementation KListAsNSArray {
  KRefSharedHolder listHolder;
}

-(void)dealloc {
  listHolder.dispose();
  [super dealloc];
}

+(id)createRetainedWithKList:(KRef)list {
  KListAsNSArray* result = [[KListAsNSArray alloc] init];
  result->listHolder.init(list);
  return result;
}

-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_OBJ(listHolder.ref<ErrorPolicy::kTerminate>());
}

-(id)objectAtIndex:(NSUInteger)index {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder kotlinValueHolder;
  KRef kotlinValue = Kotlin_List_get(listHolder.ref<ErrorPolicy::kTerminate>(), index, kotlinValueHolder.slot());
  return refToObjCOrNSNull(kotlinValue);
}

-(NSUInteger)count {
  kotlin::CalledFromNativeGuard guard;
  return Kotlin_Collection_getSize(listHolder.ref<ErrorPolicy::kTerminate>());
}

@end

@interface KMutableListAsNSMutableArray : NSMutableArray
@end

@implementation KMutableListAsNSMutableArray {
  KRefSharedHolder listHolder;
}

-(void)dealloc {
  listHolder.dispose();
  [super dealloc];
}

+(id)createRetainedWithKList:(KRef)list {
  KMutableListAsNSMutableArray* result = [[KMutableListAsNSMutableArray alloc] init];
  result->listHolder.init(list);
  return result;
}

-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_OBJ(listHolder.ref<ErrorPolicy::kTerminate>());
}

-(id)objectAtIndex:(NSUInteger)index {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder kotlinValueHolder;
  KRef kotlinValue = Kotlin_List_get(listHolder.ref<ErrorPolicy::kTerminate>(), index, kotlinValueHolder.slot());
  return refToObjCOrNSNull(kotlinValue);
}

-(NSUInteger)count {
  kotlin::CalledFromNativeGuard guard;
  return Kotlin_Collection_getSize(listHolder.ref<ErrorPolicy::kTerminate>());
}

- (void)insertObject:(id)anObject atIndex:(NSUInteger)index {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  KRef kotlinObject = refFromObjCOrNSNull(anObject, holder.slot());
  Kotlin_MutableList_addObjectAtIndex(listHolder.ref<ErrorPolicy::kTerminate>(), objCIndexToKotlinOrThrow(index), kotlinObject);
}

- (void)removeObjectAtIndex:(NSUInteger)index {
  kotlin::CalledFromNativeGuard guard;
  Kotlin_MutableList_removeObjectAtIndex(listHolder.ref<ErrorPolicy::kTerminate>(), objCIndexToKotlinOrThrow(index));
}

- (void)addObject:(id)anObject {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  Kotlin_MutableCollection_addObject(listHolder.ref<ErrorPolicy::kTerminate>(), refFromObjCOrNSNull(anObject, holder.slot()));
}

- (void)removeLastObject {
  kotlin::CalledFromNativeGuard guard;
  Kotlin_MutableList_removeLastObject(listHolder.ref<ErrorPolicy::kTerminate>());
}

- (void)replaceObjectAtIndex:(NSUInteger)index withObject:(id)anObject {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  KRef kotlinObject = refFromObjCOrNSNull(anObject, holder.slot());
  Kotlin_MutableList_setObject(listHolder.ref<ErrorPolicy::kTerminate>(), objCIndexToKotlinOrThrow(index), kotlinObject);
}

@end

@interface KSetAsNSSet : NSSet
@end

static inline id KSet_getElement(KRef set, id object) {
  if (object == NSNull.null) {
    return Kotlin_Set_contains(set, nullptr) ? object : nullptr;
  } else {
    ObjHolder requestHolder, resultHolder;
    KRef request = Kotlin_ObjCExport_refFromObjC(object, requestHolder.slot());
    KRef result = Kotlin_Set_getElement(set, request, resultHolder.slot());

    // Note: if result is nullptr, then it can't be a null element of the set, because request != nullptr;
    // so map nullptr to nullptr:
    return Kotlin_ObjCExport_refToObjC(result);
  }
}

@implementation KSetAsNSSet {
  KRefSharedHolder setHolder;
}

-(void)dealloc {
  setHolder.dispose();
  [super dealloc];
}

+(id)createRetainedWithKSet:(KRef)set {
  KSetAsNSSet* result = [[KSetAsNSSet alloc] init];
  result->setHolder.init(set);
  return result;
}

-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_OBJ(setHolder.ref<ErrorPolicy::kTerminate>());
}

-(NSUInteger) count {
  kotlin::CalledFromNativeGuard guard;
  return Kotlin_Collection_getSize(setHolder.ref<ErrorPolicy::kTerminate>());
}

- (id)member:(id)object {
  kotlin::CalledFromNativeGuard guard;
  return KSet_getElement(setHolder.ref<ErrorPolicy::kTerminate>(), object);
}

// Not mandatory, just an optimization:
- (BOOL)containsObject:(id)anObject {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  return Kotlin_Set_contains(setHolder.ref<ErrorPolicy::kTerminate>(), refFromObjCOrNSNull(anObject, holder.slot()));
}

- (NSEnumerator*)objectEnumerator {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  return [KIteratorAsNSEnumerator createWithKIterator:Kotlin_Set_iterator(setHolder.ref<ErrorPolicy::kTerminate>(), holder.slot())];
}
@end

@interface KotlinMutableSet : NSMutableSet
@end

@implementation KotlinMutableSet {
  KRefSharedHolder setHolder;
}

-(instancetype)init {
  if (self = [super init]) {
    Kotlin_initRuntimeIfNeeded();
    kotlin::ThreadStateGuard guard(kotlin::ThreadState::kRunnable);
    ObjHolder holder;
    KRef set = Kotlin_MutableSet_createWithCapacity(8, holder.slot());
    self->setHolder.init(set);
  }

  return self;
}

- (instancetype)initWithCapacity:(NSUInteger)numItems {
  if (self = [super init]) {
    Kotlin_initRuntimeIfNeeded();
    kotlin::ThreadStateGuard guard(kotlin::ThreadState::kRunnable);
    ObjHolder holder;
    KRef set = Kotlin_MutableSet_createWithCapacity(objCCapacityToKotlin(numItems), holder.slot());
    self->setHolder.init(set);
  }

  return self;
}

// TODO: super class implementation appears to be good enough.
- (instancetype)initWithObjects:(const id _Nonnull [_Nullable])objects count:(NSUInteger)cnt {
  if (self = [self initWithCapacity:cnt]) {
    for (NSUInteger i = 0; i < cnt; ++i) {
      [self addObject:objects[i]];
    }
  }

  return self;
}

// TODO: what about
// - (nullable instancetype)initWithCoder:(NSCoder *)aDecoder
// ?

-(void)dealloc {
  // Note: since setHolder initialization is not performed directly with alloc,
  // it is possible that it wasn't initialized properly.
  // Fortunately setHolder.dispose() handles the zero-initialized case too.
  setHolder.dispose();
  [super dealloc];
}

-(instancetype)initWithKSet:(KRef)set {
  if (self = [super init]) {
    setHolder.init(set);
  }

  return self;
}

-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_OBJ(setHolder.ref<ErrorPolicy::kTerminate>());
}

-(NSUInteger) count {
  kotlin::CalledFromNativeGuard guard;
  return Kotlin_Collection_getSize(setHolder.ref<ErrorPolicy::kTerminate>());
}

- (id)member:(id)object {
  kotlin::CalledFromNativeGuard guard;
  return KSet_getElement(setHolder.ref<ErrorPolicy::kTerminate>(), object);
}

// Not mandatory, just an optimization:
- (BOOL)containsObject:(id)anObject {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  return Kotlin_Set_contains(setHolder.ref<ErrorPolicy::kTerminate>(), refFromObjCOrNSNull(anObject, holder.slot()));
}

- (NSEnumerator*)objectEnumerator {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  return [KIteratorAsNSEnumerator createWithKIterator:Kotlin_Set_iterator(setHolder.ref<ErrorPolicy::kTerminate>(), holder.slot())];
}

- (void)addObject:(id)object {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  Kotlin_MutableCollection_addObject(setHolder.ref<ErrorPolicy::kTerminate>(), refFromObjCOrNSNull(object, holder.slot()));
}

- (void)removeObject:(id)object {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  Kotlin_MutableCollection_removeObject(setHolder.ref<ErrorPolicy::kTerminate>(), refFromObjCOrNSNull(object, holder.slot()));
}
@end

@interface KMapAsNSDictionary : NSDictionary
@end

static inline id KMap_get(KRef map, id aKey) {
  ObjHolder keyHolder, konstueHolder;

  KRef kotlinKey = refFromObjCOrNSNull(aKey, keyHolder.slot());
  KRef kotlinValue = Kotlin_Map_get(map, kotlinKey, konstueHolder.slot());

  if (kotlinValue == nullptr) {
    // Either null or not found.
    return Kotlin_Map_containsKey(map, kotlinKey) ? NSNull.null : nullptr;
  } else {
    return refToObjCOrNSNull(kotlinValue);
  }
}

@implementation KMapAsNSDictionary {
  KRefSharedHolder mapHolder;
}

-(void)dealloc {
  mapHolder.dispose();
  [super dealloc];
}

+(id)createRetainedWithKMap:(KRef)map {
  KMapAsNSDictionary* result = [[KMapAsNSDictionary alloc] init];
  result->mapHolder.init(map);
  return result;
}

-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_OBJ(mapHolder.ref<ErrorPolicy::kTerminate>());
}

// According to documentation, initWithObjects:forKeys:count: is required to be overridden when subclassing.
// But that doesn't make any sense, since this class can't be arbitrary initialized.

-(NSUInteger) count {
  kotlin::CalledFromNativeGuard guard;
  return Kotlin_Map_getSize(mapHolder.ref<ErrorPolicy::kTerminate>());
}

- (id)objectForKey:(id)aKey {
  kotlin::CalledFromNativeGuard guard;
  return KMap_get(mapHolder.ref<ErrorPolicy::kTerminate>(), aKey);
}

- (NSEnumerator *)keyEnumerator {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  return [KIteratorAsNSEnumerator createWithKIterator:Kotlin_Map_keyIterator(mapHolder.ref<ErrorPolicy::kTerminate>(), holder.slot())];
}

@end

@interface KotlinMutableDictionary : NSMutableDictionary
@end

@implementation KotlinMutableDictionary {
  KRefSharedHolder mapHolder;
}

-(void)dealloc {
  // Note: since mapHolder initialization is not performed directly with alloc,
  // it is possible that it wasn't initialized properly.
  // Fortunately mapHolder.dispose() handles the zero-initialized case too.
  mapHolder.dispose();
  [super dealloc];
}

-(instancetype)init {
  if (self = [super init]) {
    Kotlin_initRuntimeIfNeeded();
    kotlin::ThreadStateGuard guard(kotlin::ThreadState::kRunnable);
    ObjHolder holder;
    KRef map = Kotlin_MutableMap_createWithCapacity(8, holder.slot());
    self->mapHolder.init(map);
  }
  return self;
}

// 'initWithObjects:forKeys:count:' seems to be implemented in base class.

// TODO: what about
//  - (nullable instancetype)initWithCoder:(NSCoder *)aDecoder
// ?

- (instancetype)initWithCapacity:(NSUInteger)numItems {
  if (self = [super init]) {
    Kotlin_initRuntimeIfNeeded();
    kotlin::ThreadStateGuard guard(kotlin::ThreadState::kRunnable);
    ObjHolder holder;
    KRef map = Kotlin_MutableMap_createWithCapacity(objCCapacityToKotlin(numItems), holder.slot());
    self->mapHolder.init(map);
  }
  return self;
}

-(instancetype)initWithKMap:(KRef)map {
  if (self = [super init]) {
    mapHolder.init(map);
  }

  return self;
}

-(KRef)toKotlin:(KRef*)OBJ_RESULT {
  RETURN_OBJ(mapHolder.ref<ErrorPolicy::kTerminate>());
}

-(NSUInteger) count {
  kotlin::CalledFromNativeGuard guard;
  return Kotlin_Map_getSize(mapHolder.ref<ErrorPolicy::kTerminate>());
}

- (id)objectForKey:(id)aKey {
  kotlin::CalledFromNativeGuard guard;
  return KMap_get(mapHolder.ref<ErrorPolicy::kTerminate>(), aKey);
}

- (NSEnumerator *)keyEnumerator {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  return [KIteratorAsNSEnumerator createWithKIterator:Kotlin_Map_keyIterator(mapHolder.ref<ErrorPolicy::kTerminate>(), holder.slot())];
}

- (void)setObject:(id)anObject forKey:(id<NSCopying>)aKey {
  id keyCopy = [aKey copyWithZone:nullptr]; // Correspond to the expected NSMutableDictionary behaviour.
  {
    kotlin::CalledFromNativeGuard guard;
    ObjHolder keyHolder, konstueHolder;

    KRef kotlinKey = refFromObjCOrNSNull(keyCopy, keyHolder.slot());

    KRef kotlinValue = refFromObjCOrNSNull(anObject, konstueHolder.slot());

    Kotlin_MutableMap_set(mapHolder.ref<ErrorPolicy::kTerminate>(), kotlinKey, kotlinValue);
  }
  objc_release(keyCopy);
}

- (void)removeObjectForKey:(id)aKey {
  kotlin::CalledFromNativeGuard guard;
  ObjHolder holder;
  KRef kotlinKey = refFromObjCOrNSNull(aKey, holder.slot());

  Kotlin_MutableMap_remove(mapHolder.ref<ErrorPolicy::kTerminate>(), kotlinKey);
}

@end

@interface NSEnumerator (NSEnumeratorAsAssociatedObject)
@end

@implementation NSEnumerator (NSEnumeratorAsAssociatedObject)
-(void)releaseAsAssociatedObject {
  objc_release(self);
}
@end

// Referenced from the generated code:

extern "C" id Kotlin_Interop_CreateRetainedNSArrayFromKList(KRef obj) {
  return [KListAsNSArray createRetainedWithKList:obj];
}

extern "C" id Kotlin_Interop_CreateRetainedNSMutableArrayFromKList(KRef obj) {
  return [KMutableListAsNSMutableArray createRetainedWithKList:obj];
}

extern "C" id Kotlin_Interop_CreateRetainedNSSetFromKSet(KRef obj) {
  return [KSetAsNSSet createRetainedWithKSet:obj];
}

extern "C" id Kotlin_Interop_CreateRetainedKotlinMutableSetFromKSet(KRef obj) {
  return [[KotlinMutableSet alloc] initWithKSet:obj];
}

extern "C" id Kotlin_Interop_CreateRetainedNSDictionaryFromKMap(KRef obj) {
  return [KMapAsNSDictionary createRetainedWithKMap:obj];
}

extern "C" id Kotlin_Interop_CreateRetainedKotlinMutableDictionaryFromKMap(KRef obj) {
  return [[KotlinMutableDictionary alloc] initWithKMap:obj];
}

#endif // KONAN_OBJC_INTEROP
