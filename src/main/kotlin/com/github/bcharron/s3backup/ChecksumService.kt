package com.github.bcharron.s3backup

import java.security.MessageDigest
import java.security.DigestInputStream
import java.io.File
import java.io.FileInputStream
import org.bouncycastle.util.io.Streams

class ChecksumService() {
    fun md5(path: String): String {
        FileInputStream(path).use { input ->
            val md = MessageDigest.getInstance("MD5")
            val digestStream = DigestInputStream(input, md)

            Streams.drain(digestStream)

            return md.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
