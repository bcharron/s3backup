package com.github.bcharron.s3backup

import java.io.File
import java.security.MessageDigest
import java.security.DigestInputStream

fun String.ensureTrailingSlash(): String = this.trimEnd('/').plus('/')
