package com.github.bcharron.s3backup

import kotlin.io.path.createTempFile
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

class CryptoTest {
    companion object {
        const val EXAMPLE_PUBLIC_KEY = "src/test/resources/cert.asc"
    }

    @Test
    @DisplayName("Loading a public key should succeed")
    fun loadKeyTest() {
        val crypto = Crypto(EXAMPLE_PUBLIC_KEY)
        val cert = crypto.publicKey

        assertFalse(cert.isSecretKey())
        assertTrue(cert.getPublicKeys().size > 0)
    }

    @Test
    @DisplayName("Encryption should succeed")
    fun encryptFileTest() {
        val tempFile = createTempFile(prefix = "testPlain", suffix = ".tmp").toFile()

        tempFile.outputStream().use { out ->
            val data = "Hello!".toByteArray()
            out.write(data)
        }

        val crypto = Crypto(EXAMPLE_PUBLIC_KEY)

        val inputFile = AFile(tempFile.path, tempFile.path, tempFile.length())
        val outputFile = crypto.encrypt(inputFile)

        assertTrue(outputFile.size > 0)

        tempFile.delete()
    }

}
