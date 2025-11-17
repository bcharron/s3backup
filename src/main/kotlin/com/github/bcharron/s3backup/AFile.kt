package com.github.bcharron.s3backup

import org.bouncycastle.openpgp.api.OpenPGPCertificate
import org.bouncycastle.util.io.Streams
import org.pgpainless.PGPainless
import org.pgpainless.encryption_signing.EncryptionOptions
import org.pgpainless.encryption_signing.EncryptionStream
import org.pgpainless.encryption_signing.ProducerOptions
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class AFile(
    val path: String,
    val relativePath: String,
    val size: Long,
) {
    companion object {
        // How much the files can differ
        const val MAX_DIFFERENCE = 0.3

        fun fromPath(
            fullPath: String,
            commonPath: String,
            size: Long,
        ): AFile {
            val relative = fullPath.substring(commonPath.length).trimStart('/')

            return AFile(fullPath, relative, size)
        }
    }

    fun isSimilarSize(other: AFile): Boolean {
        val high = max(this.size, other.size) + 1f
        val low = min(this.size, other.size) + 1f
        val diff = abs(1f - low / high)

        if (diff < MAX_DIFFERENCE) {
            return true
        } else {
            return false
        }
    }

    fun encrypt(recipient: String): AFile {
        val api = PGPainless.getInstance()
        val publicKey = api.readKey().parseCertificate(recipient)

        val tempFile = createTempFile(prefix = "kotlinTemp", suffix = ".tmp").toFile()

        tempFile.outputStream().use { out ->
            File(path).inputStream().use { inputStream ->
                val encryptionOptions = EncryptionOptions.get(api).addRecipient(publicKey)
                val producerOptions = ProducerOptions.encrypt(encryptionOptions)

                api
                    .generateMessage()
                    .onOutputStream(out)
                    .withOptions(producerOptions)
                    .use { encStream ->
                        Streams.pipeAll(inputStream, encStream)
                    }
            }
        }

        return AFile.fromPath(tempFile.path, tempFile.path, tempFile.length())
    }
}
