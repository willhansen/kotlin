#import <Foundation/NSString.h>

@interface CustomString : NSString
- (instancetype)initWithValue:(int)konstue;
@property int konstue;
@end

CustomString* _Nonnull createCustomString(int konstue) {
    return [[CustomString alloc] initWithValue:konstue];
}

int getCustomStringValue(CustomString* str) {
    return str.konstue;
}

extern BOOL customStringDeallocated;