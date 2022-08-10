package com.june.strongboxkey.strongbox

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.june.strongboxkey.model.KeyPairModel
import java.math.BigInteger
import java.security.*
import java.security.spec.*

class KeyStoreUtil {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    fun isKeyInKeyStore(keyStoreAlias: String): Boolean {
        return keyStore.containsAlias(keyStoreAlias)
    }

    fun isKeyPairInKeyStore(keyStoreAlias: String): Boolean {
        val keyStoreEntry: KeyStore.Entry? = keyStore.getEntry(keyStoreAlias, null)
        return keyStoreEntry != null
    }

    //reference : Elliptic Curve Domain Parameters (https://www.secg.org/sec2-v2.pdf Page9 of 33-34)
    private fun ecParameterSpec(): ECParameterSpec {
        val p = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16)
        val a = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16)
        val b = BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16)
        val gX = BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16)
        val gY = BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16)
        val n = BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16)
        val h = 1
        val ecField = ECFieldFp(p)
        val curve = EllipticCurve(ecField, a, b)
        val g = ECPoint(gX, gY)
        return ECParameterSpec(curve, g, n, h)
    }

//[START Create Key]
    //TODO Error keyAgreement.init(myPrivateKey) -> USE Android 12, API 31 Device if not InvalidKeyException: Keystore operation failed
    fun createKeyPairInKeyStore(keyStoreAlias: String) {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            keyStoreAlias,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT or
                    KeyProperties.PURPOSE_AGREE_KEY //Field requires API level 31 (current min is 23)
        ).run {
            setUserAuthenticationRequired(false)
            ECGenParameterSpec("secp256r1") //curve type
            build()
        }
        keyPairGenerator.initialize(parameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    fun createPublicKeyByECPoint(affineX: BigInteger, affineY: BigInteger): PublicKey {
        val ecPoint = ECPoint(affineX, affineY)
        val keySpec = ECPublicKeySpec(ecPoint, ecParameterSpec())
        val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        return keyFactory.generatePublic(keySpec)
    }
//[END Create Key]

//[START Get Key]
    fun getKeyPairFromKeyStore(keyStoreAlias: String): KeyPairModel {
        val keyStoreEntry = keyStore.getEntry(keyStoreAlias, null)
        val privateKey = (keyStoreEntry as KeyStore.PrivateKeyEntry).privateKey
        val publicKey = keyStore.getCertificate(keyStoreAlias).publicKey
        return KeyPairModel(privateKey, publicKey)
    }

    fun getPublicKeyFromKeyStore(keyStoreAlias: String): PublicKey? {
        return keyStore.getCertificate(keyStoreAlias).publicKey
    }

    fun getPrivateKeyFromKeyStore(keyStoreAlias: String): PrivateKey? {
        val keyStoreEntry = keyStore.getEntry(keyStoreAlias, null)
        return (keyStoreEntry as KeyStore.PrivateKeyEntry).privateKey
    }
//[END Get Key]

//[START Delete Key]
    fun deleteKeyStoreKeyPair(keyStoreAlias: String) {
        try {
            keyStore.deleteEntry(keyStoreAlias)
        }
        catch (e: Exception) {
            throw Exception("keystore key is deleted failed $e")
        }
    }
//[END Delete Key]
}