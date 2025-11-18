package com.github.bcharron.s3backup

import org.bouncycastle.openpgp.api.OpenPGPCertificate
import java.io.File
import java.security.MessageDigest

class Backup(
    val storage: StorageProvider,
    val bucketName: String,
    val cryptoService: Crypto,
) {
    companion object {
        const val MAX_SIZE_RATIO_DIFFERENCE = 0.1
        // fun differences(entries: List<SyncEntry>) = entries.filter { it -> !it.hasRemoteFile() || !it.isSimilarSize() }
    }

    val checksumService = ChecksumService()

    private val evaluator = SyncEvaluator(MAX_SIZE_RATIO_DIFFERENCE)

    fun fileWithMetadata(file: AFile): AFile {
        val checksum = checksumService.md5(file.path)

        val metadata = mapOf("bc-checksum" to checksum)

        return AFile(file.path, file.relativePath, file.size, metadata)
    }

    suspend fun filesToSyncEntries(
        localFiles: List<AFile>,
        remoteFiles: List<AFile>,
    ): List<SyncEntry> {
        val bucketFileSet = remoteFiles.associate { it.relativePath to it }

        return localFiles.map { it ->
            val bucketEntry = bucketFileSet[it.relativePath]

            SyncEntry(it, bucketEntry, null)
        }
    }

    suspend fun encrypt(entry: SyncEntry): SyncEntry {
        val encryptedFile = cryptoService.encrypt(entry.localFile)

        return SyncEntry(entry.localFile, entry.remoteFile, encryptedFile)
    }

    suspend fun toRemotePath(
        file: AFile,
        prefix: String,
    ) = prefix.ensureTrailingSlash() + file.relativePath

    suspend fun sync(
        localPath: String,
        remotePrefix: String,
    ) {
        val bucketFiles = storage.listBucketFiles(bucketName, remotePrefix)
        val localFiles = listLocalFiles(localPath)
        val entries = filesToSyncEntries(localFiles, bucketFiles)

        entries.filter(evaluator::needsUpload).map { encrypt(it) }.forEach { entry ->
            // Check again if file size is different now that local file is encrypted
            if (evaluator.needsUpload(entry)) {
                val localFile = entry.localFile
                val encryptedFile = entry.encryptedFile!!

                println("Uploading ${localFile.relativePath}")
                println("  Size: ${localFile.sizeKB()}  Encrypted Size: ${encryptedFile.sizeKB()}")

                val dstPath = toRemotePath(localFile, remotePrefix)
                storage.upload(encryptedFile, dstPath, bucketName)
            }

            println("Deleting temp file ${entry.encryptedFile?.path}")
            entry.encryptedFile?.delete()
        }
    }

    suspend fun listLocalFiles(path: String): List<AFile> {
        val directory = File(path)

        if (!directory.exists()) {
            throw IllegalArgumentException("Directory does not exist: $path")
        }

        if (!directory.isDirectory) {
            throw IllegalArgumentException("Provided path is not a directory: $path")
        }

        return directory
            .walkTopDown()
            .filter { it.isFile }
            .map { AFile.fromPath(it.path, path, it.length()) }
            .toList()
    }
}
