package com.june.strongboxkey.strongBox.aes

import android.security.keystore.KeyProperties
import android.util.Base64
import com.june.strongboxkey.strongBox.StrongBoxConstants
import com.june.strongboxkey.strongBox.StrongBoxConstants.CIPHER_AES_ECB_PADDING
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESEncryption {
    fun encryptionCBCMode(userInput: String, hash: ByteArray): String {
        val input: ByteArray = userInput.toByteArray()
        val key: Key = SecretKeySpec(
            hash,
            KeyProperties.KEY_ALGORITHM_AES
        )
        val cipher = Cipher.getInstance(StrongBoxConstants.CIPHER_AES_CBC_PADDING)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            key,
            IvParameterSpec(StrongBoxConstants.iv)
        )
        val result: String
        cipher.doFinal(input).let { bytes ->
            result = Base64.encodeToString(bytes, Base64.DEFAULT)
        }
        return result
    }

    fun encryptionECBMode(userInput: String, hash: ByteArray): String {
        val input: ByteArray = userInput.toByteArray()
        val key: Key = SecretKeySpec(
            hash,
            KeyProperties.KEY_ALGORITHM_AES
        )
        val cipher = Cipher.getInstance(CIPHER_AES_ECB_PADDING)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            key
        )
        val result: String
        cipher.doFinal(input).let { bytes ->
            result = Base64.encodeToString(bytes, Base64.DEFAULT)
        }
        return result
    }
}