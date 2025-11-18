package com.github.bcharron.s3backup

fun String.ensureTrailingSlash(): String = this.trimEnd('/').plus('/')

