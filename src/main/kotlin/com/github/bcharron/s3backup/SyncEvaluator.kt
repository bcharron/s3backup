package com.github.bcharron.s3backup

import org.bouncycastle.openpgp.api.OpenPGPCertificate
import kotlin.math.abs

class SyncEvaluator(val allowedSizeDifference: Double) {
    fun needsUpload(entry: SyncEntry): Boolean {
        // File doesn't exist in bucket
        if (entry.remoteFile == null) {
            return true
        }

        // We have an encrypted file, check that one beacuse encrypted size will usually be
        // different due to metadata and compression
        if (entry.encryptedFile != null) {
            return !isSimilarSize(entry.encryptedFile, entry.remoteFile)
        }

        return !isSimilarSize(entry.localFile, entry.remoteFile)
    }

    fun isSimilarSize(a: AFile, b: AFile): Boolean {
        val pctDifference = 1.0 - sizeRatio(a.size, b.size)

        return pctDifference < allowedSizeDifference
    }

    private fun sizeRatio(
        a: Long,
        b: Long,
    ): Double {
        if (a == 0L && b == 0L) return 1.0

        val min = minOf(a, b).toDouble()
        val max = maxOf(a, b).toDouble()

        return abs(min / max)
    }
}
