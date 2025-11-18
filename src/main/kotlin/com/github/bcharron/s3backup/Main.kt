package com.github.bcharron.s3backup

import aws.sdk.kotlin.services.s3.S3Client
import kotlin.system.exitProcess
import java.io.File

suspend fun main(args: Array<String>) {
    if (args.size != 4) {
        println("Usage: s3backup <local directory> <remote prefix> <bucket name> <recipient gpg public key path>")
        exitProcess(1)
    }

    val (localDirectory, remotePrefix, bucketName, recipientKeyPath) = args

    val s3Storage = S3Storage.create()
    // val publicKey = Crypto.loadPublicKey(recipientKeyPath)
    val cryptoService = Crypto(recipientKeyPath)

    val backup = Backup(s3Storage, bucketName, cryptoService)

    backup.sync(localDirectory, remotePrefix)
}

