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

#if KONAN_OBJC_INTEROP

#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSException.h>
#import <Foundation/NSString.h>

#import "ObjCExport.h"
#import "Runtime.h"
#import "Mutex.hpp"

extern "C" {

OBJ_GETTER(Kotlin_boxBoolean, KBoolean konstue);
OBJ_GETTER(Kotlin_boxByte, KByte konstue);
OBJ_GETTER(Kotlin_boxShort, KShort konstue);
OBJ_GETTER(Kotlin_boxInt, KInt konstue);
OBJ_GETTER(Kotlin_boxLong, KLong konstue);
OBJ_GETTER(Kotlin_boxUByte, KUByte konstue);
OBJ_GETTER(Kotlin_boxUShort, KUShort konstue);
OBJ_GETTER(Kotlin_boxUInt, KUInt konstue);
OBJ_GETTER(Kotlin_boxULong, KULong konstue);
OBJ_GETTER(Kotlin_boxFloat, KFloat konstue);
OBJ_GETTER(Kotlin_boxDouble, KDouble konstue);

}

#pragma clang diagnostic ignored "-Wobjc-designated-initializers"

@interface KotlinNumber : NSNumber
@end

[[ noreturn ]] static void incorrectNumberInitialization(KotlinNumber* self, SEL _cmd) {
  [NSException raise:NSGenericException format:@"%@ can't be initialized with %s, use properly typed initialized",
    NSStringFromClass([self class]), sel_getName(_cmd)];

  abort();
}

[[ noreturn ]] static void incorrectNumberFactory(Class self, SEL _cmd) {
  [NSException raise:NSGenericException format:@"%@ can't be created with %s, use properly typed factory",
    NSStringFromClass(self), sel_getName(_cmd)];

  abort();
}

@implementation KotlinNumber : NSNumber

- (NSNumber *)initWithBool:(BOOL)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithChar:(char)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithShort:(short)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithInt:(int)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithInteger:(NSInteger)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithLong:(long)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithLongLong:(long long)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithUnsignedChar:(unsigned char)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithUnsignedShort:(unsigned short)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithUnsignedInt:(unsigned int)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithUnsignedInteger:(NSUInteger)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithUnsignedLong:(unsigned long)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithUnsignedLongLong:(unsigned long long)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithFloat:(float)konstue { incorrectNumberInitialization(self, _cmd); }
- (NSNumber *)initWithDouble:(double)konstue { incorrectNumberInitialization(self, _cmd); }

+ (NSNumber *)numberWithBool:(BOOL)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithChar:(char)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithShort:(short)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithInt:(int)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithInteger:(NSInteger)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithLong:(long)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithLongLong:(long long)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithUnsignedChar:(unsigned char)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithUnsignedShort:(unsigned short)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithUnsignedInt:(unsigned int)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithUnsignedLong:(unsigned long)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithUnsignedInteger:(NSUInteger)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithUnsignedLongLong:(unsigned long long)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithFloat:(float)konstue { incorrectNumberFactory(self, _cmd); }
+ (NSNumber *)numberWithDouble:(double)konstue { incorrectNumberFactory(self, _cmd); }

@end

/*
The code below is generated by:


fun main(args: Array<String>) {
    println(genBoolean())
    println(genInteger("Byte", "c", "char"))
    println(genInteger("Short", "s", "short"))
    println(genInteger("Int", "i", "int"))
    println(genInteger("Long", "q", "long long"))
    println(genInteger("UByte", "C", "unsigned char"))
    println(genInteger("UShort", "S", "unsigned short"))
    println(genInteger("UInt", "I", "unsigned int"))
    println(genInteger("ULong", "Q", "unsigned long long"))
    println(genFloating("Float", "f", "float"))
    println(genFloating("Double", "d", "double"))
}

private fun genBoolean(): String = """
@interface KotlinBoolean : KotlinNumber
@end

@implementation KotlinBoolean {
  BOOL konstue_;
}

- (void)getValue:(void *)konstue {
	*(BOOL*)konstue = konstue_;
}

- (instancetype)initWithBool:(BOOL)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithBool:(BOOL)konstue {
  KotlinBoolean* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

- (BOOL)boolValue {
  return konstue_;
}

- (char)charValue {
  return konstue_;
}

- (const char *)objCType {
  return "c";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxBoolean, konstue_);
}

@end

""".trimIndent()

private fun genInteger(
        name: String,
        encoding: String,
        cType: String,
        kind: String = getNSNumberKind(cType)
) = """
@interface Kotlin$name : KotlinNumber
@end

@implementation Kotlin$name {
  $cType konstue_;
}

- (void)getValue:(void *)konstue {
	*($cType*)konstue = konstue_;
}

- (instancetype)initWith${kind.capitalize()}:($cType)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWith${kind.capitalize()}:($cType)konstue {
  Kotlin$name* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- ($cType)${kind}Value {
  return konstue_;
}

- (const char *)objCType {
  return "$encoding";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_box$name, konstue_);
}

@end

""".trimIndent()

private fun getNSNumberKind(cType: String) =
        cType.split(' ').joinToString("") { it.capitalize() }.decapitalize()

private fun genFloating(
        name: String,
        encoding: String,
        cType: String,
        kind: String = getNSNumberKind(cType)
): String = """
@interface Kotlin$name : KotlinNumber
@end

@implementation Kotlin$name {
  $cType konstue_;
}

- (void)getValue:(void *)konstue {
	*($cType*)konstue = konstue_;
}

- (instancetype)initWith${kind.capitalize()}:($cType)konstue  {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWith${kind.capitalize()}:($cType)konstue {
  Kotlin$name* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}
${if (cType != "double") """
// Required to convert Swift floating literals.
- (instancetype)initWithDouble:(double)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}
""" else ""}
- ($cType)${kind}Value {
  return konstue_;
}

- (const char *)objCType {
  return "$encoding";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_box$name, konstue_);
}

@end

""".trimIndent()
*/

// TODO: consider generating it by compiler.

@interface KotlinBoolean : KotlinNumber
@end

@implementation KotlinBoolean {
  BOOL konstue_;
}

- (void)getValue:(void *)konstue {
	*(BOOL*)konstue = konstue_;
}

- (instancetype)initWithBool:(BOOL)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithBool:(BOOL)konstue {
  KotlinBoolean* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

- (BOOL)boolValue {
  return konstue_;
}

- (char)charValue {
  return konstue_;
}

- (const char *)objCType {
  return "c";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxBoolean, konstue_);
}

@end

@interface KotlinByte : KotlinNumber
@end

@implementation KotlinByte {
  char konstue_;
}

- (void)getValue:(void *)konstue {
	*(char*)konstue = konstue_;
}

- (instancetype)initWithChar:(char)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithChar:(char)konstue {
  KotlinByte* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (char)charValue {
  return konstue_;
}

- (const char *)objCType {
  return "c";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxByte, konstue_);
}

@end

@interface KotlinShort : KotlinNumber
@end

@implementation KotlinShort {
  short konstue_;
}

- (void)getValue:(void *)konstue {
	*(short*)konstue = konstue_;
}

- (instancetype)initWithShort:(short)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithShort:(short)konstue {
  KotlinShort* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (short)shortValue {
  return konstue_;
}

- (const char *)objCType {
  return "s";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxShort, konstue_);
}

@end

@interface KotlinInt : KotlinNumber
@end

@implementation KotlinInt {
  int konstue_;
}

- (void)getValue:(void *)konstue {
	*(int*)konstue = konstue_;
}

- (instancetype)initWithInt:(int)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithInt:(int)konstue {
  KotlinInt* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (int)intValue {
  return konstue_;
}

- (const char *)objCType {
  return "i";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxInt, konstue_);
}

@end

@interface KotlinLong : KotlinNumber
@end

@implementation KotlinLong {
  long long konstue_;
}

- (void)getValue:(void *)konstue {
	*(long long*)konstue = konstue_;
}

- (instancetype)initWithLongLong:(long long)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithLongLong:(long long)konstue {
  KotlinLong* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (long long)longLongValue {
  return konstue_;
}

- (const char *)objCType {
  return "q";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxLong, konstue_);
}

@end

@interface KotlinUByte : KotlinNumber
@end

@implementation KotlinUByte {
  unsigned char konstue_;
}

- (void)getValue:(void *)konstue {
	*(unsigned char*)konstue = konstue_;
}

- (instancetype)initWithUnsignedChar:(unsigned char)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithUnsignedChar:(unsigned char)konstue {
  KotlinUByte* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (unsigned char)unsignedCharValue {
  return konstue_;
}

- (const char *)objCType {
  return "C";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxUByte, konstue_);
}

@end

@interface KotlinUShort : KotlinNumber
@end

@implementation KotlinUShort {
  unsigned short konstue_;
}

- (void)getValue:(void *)konstue {
	*(unsigned short*)konstue = konstue_;
}

- (instancetype)initWithUnsignedShort:(unsigned short)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithUnsignedShort:(unsigned short)konstue {
  KotlinUShort* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (unsigned short)unsignedShortValue {
  return konstue_;
}

- (const char *)objCType {
  return "S";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxUShort, konstue_);
}

@end

@interface KotlinUInt : KotlinNumber
@end

@implementation KotlinUInt {
  unsigned int konstue_;
}

- (void)getValue:(void *)konstue {
	*(unsigned int*)konstue = konstue_;
}

- (instancetype)initWithUnsignedInt:(unsigned int)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithUnsignedInt:(unsigned int)konstue {
  KotlinUInt* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (unsigned int)unsignedIntValue {
  return konstue_;
}

- (const char *)objCType {
  return "I";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxUInt, konstue_);
}

@end

@interface KotlinULong : KotlinNumber
@end

@implementation KotlinULong {
  unsigned long long konstue_;
}

- (void)getValue:(void *)konstue {
	*(unsigned long long*)konstue = konstue_;
}

- (instancetype)initWithUnsignedLongLong:(unsigned long long)konstue {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)konstue {
  KotlinULong* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (unsigned long long)unsignedLongLongValue {
  return konstue_;
}

- (const char *)objCType {
  return "Q";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxULong, konstue_);
}

@end

@interface KotlinFloat : KotlinNumber
@end

@implementation KotlinFloat {
  float konstue_;
}

- (void)getValue:(void *)konstue {
	*(float*)konstue = konstue_;
}

- (instancetype)initWithFloat:(float)konstue  {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithFloat:(float)konstue {
  KotlinFloat* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

// Required to convert Swift floating literals.
- (instancetype)initWithDouble:(double)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (float)floatValue {
  return konstue_;
}

- (const char *)objCType {
  return "f";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxFloat, konstue_);
}

@end

@interface KotlinDouble : KotlinNumber
@end

@implementation KotlinDouble {
  double konstue_;
}

- (void)getValue:(void *)konstue {
	*(double*)konstue = konstue_;
}

- (instancetype)initWithDouble:(double)konstue  {
  self = [super init];
  konstue_ = konstue;
  return self;
}

+ (instancetype)numberWithDouble:(double)konstue {
  KotlinDouble* result = [[self new] autorelease];
  result->konstue_ = konstue;
  return result;
}

// Required to convert Swift integer literals.
- (instancetype)initWithInteger:(NSInteger)konstue {
  self = [super init];
  konstue_ = konstue; // TODO: check fits.
  return self;
}

- (double)doubleValue {
  return konstue_;
}

- (const char *)objCType {
  return "d";
}

-(ObjHeader*)toKotlin:(ObjHeader**)OBJ_RESULT {
  RETURN_RESULT_OF(Kotlin_boxDouble, konstue_);
}

@end

#endif // KONAN_OBJC_INTEROP
