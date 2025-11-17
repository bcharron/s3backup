package com.github.bcharron.s3backup

import aws.sdk.kotlin.services.s3.S3Client
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {
    val client = S3Client.fromEnvironment { region = "us-east-1" }
    val s3backup = S3Backup(client)

    if (args.size != 3) {
        println("Usage: s3backup <local directory> <remote prefix> <bucket name>")
        exitProcess(1)
    }

    val (localDirectory, remotePrefix, bucketName) = args

    s3backup.sync(localDirectory, bucketName, remotePrefix)
}

