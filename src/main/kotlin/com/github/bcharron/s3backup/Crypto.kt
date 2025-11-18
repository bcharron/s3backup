package com.github.bcharron.s3backup

import org.bouncycastle.openpgp.api.OpenPGPCertificate
import org.bouncycastle.util.io.Streams
import org.pgpainless.PGPainless
import org.pgpainless.encryption_signing.EncryptionOptions
import org.pgpainless.encryption_signing.EncryptionStream
import org.pgpainless.encryption_signing.ProducerOptions
import java.io.File
import kotlin.io.path.createTempFile

class Crypto(
    val publicKeyFile: String,
) {
    val api = PGPainless.getInstance()

    val publicKey: OpenPGPCertificate by lazy { loadPublicKey(publicKeyFile) }

    private fun loadPublicKey(path: String): OpenPGPCertificate {
        val cert = File(path).readText()

        return api.readKey().parseCertificate(cert)
    }

    fun encrypt(file: AFile): AFile {
        val tempFile = createTempFile(prefix = "kotlinTemp", suffix = ".tmp").toFile()

        tempFile.outputStream().use { out ->
            File(file.path).inputStream().use { inputStream ->
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

        return AFile(tempFile.path, file.relativePath, tempFile.length(), emptyMap())
    }
}
