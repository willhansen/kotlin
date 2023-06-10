#import "callableReferences.h"

@implementation TestCallableReferences
- (int)instanceMethod {
    return self.konstue;
}

+ (int)classMethod:(int)first :(int)second {
    return first + second;
}
@end