package com.github.bcharron.s3backup

interface StorageProvider {
    suspend fun upload(
        src: AFile,
        remotePath: String,
        bucketName: String,
    )

    
    suspend fun listBucketFiles(
        bucketName: String,
        prefix: String,
    ): List<AFile>
}
