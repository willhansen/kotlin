#import <Foundation/NSObject.h>

@interface TestOverrideInit : NSObject
-(instancetype)initWithValue:(int)konstue NS_DESIGNATED_INITIALIZER;
+(instancetype)createWithValue:(int)konstue;
@end