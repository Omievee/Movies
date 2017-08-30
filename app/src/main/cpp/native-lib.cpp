//
// Created by Ryan McManus on 7/12/17.
//

#include <jni.h>

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_moviepass_network_RestClient_getEndPoint(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("https://android.moviepass.com");
        return str;
    }

    JNIEXPORT jstring JNICALL
    Java_com_moviepass_fragments_SignUpStepTwoFragment_getSandboxTokenizationKey(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("sandbox_snpbh3w8_2fj37y3ndsjstzxv");
        return str;
    }

    JNIEXPORT jstring JNICALL
    Java_com_moviepass_fragments_SignUpStepTwoFragment_getProductionTokenizationKey(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("sandbox_snpbh3w8_2fj37y3ndsjstzxv");
        return str;
    }

    JNIEXPORT jstring JNICALL
    Java_com_moviepass_application_Application_getCognitoKey(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("us-east-1:4b4b52ee-11c2-4c56-b109-b654a1d2ad37");
        return str;
    }

    JNIEXPORT jstring JNICALL
    Java_com_moviepass_activities_VerificationPictureActivity_getProductionBucket(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("mpcore.production.tickets");
        return str;
    }

    JNIEXPORT jstring JNICALL
    Java_com_moviepass_activities_VerificationPictureActivity_getStagingBucket(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("mpcore.staging.tickets");
        return str;
    }

    JNIEXPORT jstring JNICALL
    Java_com_moviepass_fragments_ProfilePaymentInformationFragment_getSandboxTokenizationKey(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("sandbox_snpbh3w8_2fj37y3ndsjstzxv");
        return str;
    }

    JNIEXPORT jstring JNICALL
    Java_com_moviepass_fragments_ProfilePaymentInformationFragment_getProductionTokenizationKey(JNIEnv *env, jclass) {
        jstring str = env->NewStringUTF("sandbox_snpbh3w8_2fj37y3ndsjstzxv");
        return str;
    }
}