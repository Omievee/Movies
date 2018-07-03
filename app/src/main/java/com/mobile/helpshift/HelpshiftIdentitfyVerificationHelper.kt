package com.mobile.helpshift

import android.util.Base64
import com.helpshift.HelpshiftUser
import com.mobile.UserPreferences
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HelpshiftIdentitfyVerificationHelper {

    companion object {

        fun getHelpshiftUser(): HelpshiftUser {
            val helpshiftUser: HelpshiftUser.Builder = HelpshiftUser.Builder(UserPreferences.userId.toString(),
                    UserPreferences.userEmail).setName(UserPreferences.userName)
            return helpshiftUser.build()
        }

        private fun sign(helpshiftUser: HelpshiftUser, secretKey:String): String? {
            return try {
                val hmacsha256 = Mac.getInstance("HmacSHA256")
                hmacsha256.init(SecretKeySpec(secretKey.toByteArray(), "HmacSHA256"));
                val msg = arrayOf(helpshiftUser.identifier ?: "", helpshiftUser.email
                        ?: "").joinToString()
                val hash = hmacsha256.doFinal(msg.toByteArray());
                Base64.encodeToString(hash, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    }
}