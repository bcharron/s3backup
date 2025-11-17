package com.github.bcharron.s3backup

import aws.sdk.kotlin.services.s3.model.ListObjectsRequest
import aws.sdk.kotlin.services.s3.S3Client
import java.io.File

class S3Backup(val client: S3Client) {
    companion object {
        fun ensureTrailingSlash(s: String) = s.trimEnd('/').plus('/')
    }

    suspend fun sync(localPath: String, bucketName: String, remotePath: String) {
        val bucketFiles = listBucketFiles(bucketName, remotePath)
        val localFiles = listLocalFiles(localPath)

        println("Bucket files:")

        val localFileSet = localFiles.associate { it.relativePath to it }
        val bucketFileSet = bucketFiles.associate { it.relativePath to it }

        localFileSet.forEach { k, v ->
            bucketFileSet[k]?.equals(v)
        }

        bucketFiles.forEach { println(it) }
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

    suspend fun listBucketFiles(bucketName: String, prefix: String): List<AFile> {
        val cleanPrefix = ensureTrailingSlash(prefix)

        val req = ListObjectsRequest {
            bucket = bucketName
            this.prefix = cleanPrefix
        }

        client.use { s3 ->
            val response = s3.listObjects(req)

            return response.contents
                    .orEmpty()
                    .filter { it.key?.startsWith(cleanPrefix) == true && it.size != null }
                    .map { AFile.fromPath(it.key!!, cleanPrefix, it.size!!) }
        }
    }
}
