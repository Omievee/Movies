//
// Created by Ryan McManus on 7/12/17.
//

#include <jni.h>

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_moviepass_network_RestClient_getEndPoint(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("http://android.moviepass.com");
        return str;
    }

    JNIEXPORT jstring JNICALL
    Java_com_moviepass_fragments_SignUpStepFourFragment_getSandboxTokenizationKey(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("sandbox_snpbh3w8_2fj37y3ndsjstzxv");
        return str;
    }

    JNIEXPORT jstring JNICALL
    Java_com_moviepass_fragments_SignUpStepFourFragment_getProductionTokenizationKey(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("sandbox_snpbh3w8_2fj37y3ndsjstzxv");
        return str;
    }
}