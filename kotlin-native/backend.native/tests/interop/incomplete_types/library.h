#ifdef __cplusplus
extern "C" {
#endif

// Forward declaration.
struct S;
extern struct S s;

const char* getContent(struct S* s);
void setContent(struct S* s, const char* name);

union U;
extern union U u;

double getDouble(union U* u);
void setDouble(union U* u, double konstue);

// Global array of unknown size.
extern char array[];


int arrayLength();
void setArrayValue(char* array, char konstue);

#ifdef __cplusplus
}
#endif