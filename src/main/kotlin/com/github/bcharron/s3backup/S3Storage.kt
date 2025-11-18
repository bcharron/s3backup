package com.github.bcharron.s3backup

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ListObjectsRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import java.io.File

class S3Storage private constructor(
    private val client: S3Client,
) : StorageProvider {
    companion object {
        suspend fun create(): S3Storage {
            val s3Client = S3Client.fromEnvironment { }

            return S3Storage(s3Client)
        }
    }

    override suspend fun upload(
        src: AFile,
        remotePath: String,
        bucketName: String,
    ) {
        println("  upload to s3://${bucketName}/${remotePath}")

        val req =
            PutObjectRequest {
                bucket = bucketName
                body = File(src.path).asByteStream()
                key = remotePath
                // metadata = src.customMetadata
            }

        client.putObject(req)
    }

    override suspend fun listBucketFiles(
        bucketName: String,
        prefix: String,
    ): List<AFile> {
        val cleanPrefix = prefix.ensureTrailingSlash()

        val req =
            ListObjectsRequest {
                bucket = bucketName
                this.prefix = cleanPrefix
            }

        val response = client.listObjects(req)

        return response.contents
            .orEmpty()
            .filter { it.key?.startsWith(cleanPrefix) == true && it.size != null }
            .map { AFile.fromPath(it.key!!, cleanPrefix, it.size!!) }
    }
}
