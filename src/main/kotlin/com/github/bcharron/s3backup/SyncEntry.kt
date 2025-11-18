package com.github.bcharron.s3backup

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import org.bouncycastle.openpgp.api.OpenPGPCertificate

data class SyncEntry(val localFile: AFile, val remoteFile: AFile?, val encryptedFile: AFile?)
