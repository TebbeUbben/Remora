package de.tebbeubben.remora.lib.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import dagger.Reusable
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.math.min

@Reusable
internal class Crypto @Inject constructor() {

    fun generateECDHKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("EC")
        keyGen.initialize(ECGenParameterSpec("prime256v1"))
        return keyGen.generateKeyPair()
    }

    fun deriveECDHSecret(privateKey: PrivateKey, publicKey: PublicKey): ByteArray {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(publicKey, true)
        return keyAgreement.generateSecret()
    }

    fun decodeECDHPublicKey(encoded: ByteArray): PublicKey =
        KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(encoded))

    private fun storeKey(alias: String, key: ByteArray, purpose: Int) {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey: SecretKey = SecretKeySpec(key, "AES")
        val protection: KeyStore.ProtectionParameter =
            KeyProtection.Builder(purpose)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build()
        keyStore.setEntry(alias, KeyStore.SecretKeyEntry(secretKey), protection)
    }

    fun storeIngoingKey(peerDeviceId: Long, key: ByteArray) {
        storeKey(INGOING_KEY_ALIAS_PREFIX + peerDeviceId, key, KeyProperties.PURPOSE_DECRYPT)
    }

    fun storeOutgoingKey(peerDeviceId: Long, key: ByteArray) {
        storeKey(OUTGOING_KEY_ALIAS_PREFIX + peerDeviceId, key, KeyProperties.PURPOSE_ENCRYPT)
    }

    fun storeIngoingStatusKey(peerDeviceId: Long, key: ByteArray) {
        storeKey(STATUS_KEY_ALIAS_PREFIX + peerDeviceId, key, KeyProperties.PURPOSE_DECRYPT)
    }

    fun storeOutgoingStatusKey(peerDeviceId: Long, key: ByteArray) {
        storeKey(STATUS_KEY_ALIAS_PREFIX + peerDeviceId, key, KeyProperties.PURPOSE_ENCRYPT)
    }

    fun decryptAESCTR(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(data)
    }

    fun decryptStatusKeyAESGCM(peerDeviceId: Long, iv: ByteArray, data: ByteArray, aad: ByteArray) =
        decryptAESGCM(STATUS_KEY_ALIAS_PREFIX + peerDeviceId, iv, data, aad)

    fun decryptMessageAESGCM(peerDeviceId: Long, iv: ByteArray, data: ByteArray, aad: ByteArray) =
        decryptAESGCM(INGOING_KEY_ALIAS_PREFIX + peerDeviceId, iv, data, aad)

    fun decryptAESGCM(keyAlias: String, iv: ByteArray, data: ByteArray, aad: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        return decryptAESGCM(secretKey, iv, data, aad)
    }

    fun decryptAESGCM(secretKey: SecretKey, iv: ByteArray, data: ByteArray, aad: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        cipher.updateAAD(aad)
        return cipher.doFinal(data)
    }

    fun encryptAESCTR(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(data)
    }

    fun encryptStatusKeyAESGCM(peerDeviceId: Long, iv: ByteArray, data: ByteArray, aad: ByteArray) =
        encryptAESGCM(STATUS_KEY_ALIAS_PREFIX + peerDeviceId, iv, data, aad)

    fun encryptMessageAESGCM(peerDeviceId: Long, iv: ByteArray, data: ByteArray, aad: ByteArray) =
        encryptAESGCM(OUTGOING_KEY_ALIAS_PREFIX + peerDeviceId, iv, data, aad)

    fun encryptAESGCM(keyAlias: String, iv: ByteArray, data: ByteArray, aad: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        return encryptAESGCM(secretKey, iv, data, aad)
    }

    fun encryptAESGCM(secretKey: SecretKey, iv: ByteArray, data: ByteArray, aad: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        cipher.updateAAD(aad)
        return cipher.doFinal(data)
    }

    private fun generateDatabaseAesKeyIfNeeded() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey(DATABASE_AES_KEY_ALIAS, null) as SecretKey?
        if (secretKey != null) return
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGen.init(
            KeyGenParameterSpec.Builder(
                DATABASE_AES_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        keyGen.generateKey()
    }

    fun encryptECDHPrivateKey(privateKey: PrivateKey): ByteArray {
        generateDatabaseAesKeyIfNeeded()
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey(DATABASE_AES_KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.iv + cipher.doFinal(privateKey.encoded)
    }

    fun decryptECDHPrivateKey(encryptedKey: ByteArray): PrivateKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey(DATABASE_AES_KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = encryptedKey.copyOfRange(0, 12)
        val data = encryptedKey.copyOfRange(12, encryptedKey.size)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val decoded = cipher.doFinal(data)
        val keySpec = PKCS8EncodedKeySpec(decoded)
        return KeyFactory.getInstance("EC").generatePrivate(keySpec)
    }

    fun decodeRSAPrivateKey(encoded: ByteArray): PrivateKey =
        KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(encoded))

    fun hkdf(
        ikm: ByteArray,
        salt: ByteArray?,
        info: ByteArray,
        outputLength: Int,
        algorithm: String = "HmacSHA256"
    ): ByteArray {
        // Extract
        val mac = Mac.getInstance(algorithm)
        mac.init(
            SecretKeySpec(salt?.takeIf { it.isNotEmpty() } ?: ByteArray(mac.macLength),
                          algorithm)
        )
        val prk = mac.doFinal(ikm)

        // Expand
        mac.init(SecretKeySpec(prk, algorithm))

        val hashLen = mac.macLength
        val n = (outputLength + hashLen - 1) / hashLen
        require(n <= 255) { "Output length too long" }

        var t = ByteArray(0)
        val okm = ByteArray(outputLength)
        var pos = 0

        for (i in 1..n) {
            mac.update(t)
            mac.update(info)
            mac.update(i.toByte())
            t = mac.doFinal()
            val toCopy = min(hashLen, outputLength - pos)
            t.copyInto(okm, pos, 0, toCopy)
            pos += toCopy
        }

        Arrays.fill(prk, 0)
        Arrays.fill(t, 0)

        return okm
    }

    companion object {

        const val DATABASE_AES_KEY_ALIAS = "remora-database"
        const val INGOING_KEY_ALIAS_PREFIX = "remora-ingoing-"
        const val OUTGOING_KEY_ALIAS_PREFIX = "remora-outgoing-"

        const val STATUS_KEY_ALIAS_PREFIX = "remora-status-"

    }
}