#include <jni.h>
#include <string>
#include "Dalvik.h"
#include "art_method.h"

typedef Object *(*FindObject)(void *thread, jobject jobject1);
typedef  void* (*FindThread)();
FindObject  findObject;
FindThread  findThread;

extern "C" JNIEXPORT jstring JNICALL
Java_com_jason_andfix_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_jason_andfix_DexFileManager_replaceArt(JNIEnv *env, jobject thiz, jobject wrong_method,
                                                jobject right_method) {
//    art虚拟机替换  art  ArtMethod  ---》Java方法
    art::mirror::ArtMethod *wrong = (art::mirror::ArtMethod *) env->FromReflectedMethod(wrong_method);
    art::mirror::ArtMethod *right = (art::mirror::ArtMethod *) env->FromReflectedMethod(right_method);




    wrong->declaring_class_=right->declaring_class_;

    wrong->dex_code_item_offset_=right->dex_code_item_offset_;
    wrong->method_index_=right->method_index_;
    wrong->dex_method_index_=right->dex_method_index_;


    //入口
    wrong->ptr_sized_fields_.entry_point_from_jni_=right->ptr_sized_fields_.entry_point_from_jni_;
    //    机器码模式
    wrong->ptr_sized_fields_.entry_point_from_quick_compiled_code_=right->ptr_sized_fields_.entry_point_from_quick_compiled_code_;

    //  不一样
    //wrong->ptr_sized_fields_.entry_point_from_jni_=right->ptr_sized_fields_.entry_point_from_jni_;
    //wrong->ptr_sized_fields_.dex_cache_resolved_methods_=right->ptr_sized_fields_.dex_cache_resolved_methods_;
    //wrong->ptr_sized_fields_.dex_cache_resolved_types_=right->ptr_sized_fields_.dex_cache_resolved_types_;
    //wrong->hotness_count_=right->hotness_count_;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_jason_andfix_DexFileManager_replaceDalvik(JNIEnv *env, jobject thiz, jint sdk,
                                                   jobject wrong_method, jobject right_method) {
    //做  跟什么有关   虚拟机    java虚拟机 Method
    //找到虚拟机对应的Method 结构体
    Method *wrong = (Method *) env->FromReflectedMethod(wrong_method);

    Method *right =(Method *) env->FromReflectedMethod(right_method);

    //下一步  把right 对应Object   第一个成员变量ClassObject   status


    //ClassObject
    void *dvm_hand=dlopen("libdvm.so", RTLD_NOW);
    //sdk  10    以前是这样   10会发生变化
    findObject= (FindObject) dlsym(dvm_hand, sdk > 10 ?
                                             "_Z20dvmDecodeIndirectRefP6ThreadP8_jobject" :
                                             "dvmDecodeIndirectRef");
    findThread = (FindThread) dlsym(dvm_hand, sdk > 10 ? "_Z13dvmThreadSelfv" : "dvmThreadSelf");
    // method   所声明的Class

    jclass methodClaz = env->FindClass("java/lang/reflect/Method");
    jmethodID rightMethodId = env->GetMethodID(methodClaz, "getDeclaringClass",
                                               "()Ljava/lang/Class;");
    //dalvik  odex   机器码
    //  firstFiled->status=CLASS_INITIALIZED
    //  art不需要    dalvik适配
    jobject ndkObject = env->CallObjectMethod(right_method, rightMethodId);
    ClassObject *firstFiled = (ClassObject *) findObject(findThread(), ndkObject);
    firstFiled->status=CLASS_INITIALIZED;
    wrong->accessFlags |= ACC_PUBLIC;

    wrong->methodIndex=right->methodIndex;
    wrong->jniArgInfo=right->jniArgInfo;
    wrong->registersSize=right->registersSize;
    wrong->outsSize=right->outsSize;
//    方法参数 原型
    wrong->prototype=right->prototype;
//
    wrong->insns=right->insns;
    wrong->nativeFunc=right->nativeFunc;
}