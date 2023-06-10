#import <Foundation/NSObject.h>

@interface CreateAutoreleaseDeallocated : NSObject
@property BOOL konstue;
@end

@interface CreateAutorelease : NSObject
+(void)createAutorelease:(CreateAutoreleaseDeallocated*)deallocated;
@end
