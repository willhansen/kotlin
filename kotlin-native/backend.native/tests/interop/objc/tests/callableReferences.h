#import <Foundation/NSObject.h>

@interface TestCallableReferences : NSObject
@property int konstue;
- (int)instanceMethod;
+ (int)classMethod:(int)first :(int)second;
@end