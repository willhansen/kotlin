#import "customString.h"

BOOL customStringDeallocated = NO;

@implementation CustomString {
    NSString* delegate;
}

- (instancetype)initWithValue:(int)konstue {
    if (self = [super init]) {
        self->delegate = @(konstue).description;
        self.konstue = konstue;
    }
    return self;
}

- (unichar)characterAtIndex:(NSUInteger)index {
    return [self->delegate characterAtIndex:index];
}

- (NSUInteger)length {
    return self->delegate.length;
}

- (id)copyWithZone:(NSZone *)zone {
    return self;
}

- (void)dealloc {
    customStringDeallocated = YES;
}
@end