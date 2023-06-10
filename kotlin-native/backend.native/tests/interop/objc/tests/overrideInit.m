#import "overrideInit.h"

@implementation TestOverrideInit
-(instancetype)initWithValue:(int)konstue {
    return self = [super init];
}

+(instancetype)createWithValue:(int)konstue {
    return [[self alloc] initWithValue:konstue];
}
@end
