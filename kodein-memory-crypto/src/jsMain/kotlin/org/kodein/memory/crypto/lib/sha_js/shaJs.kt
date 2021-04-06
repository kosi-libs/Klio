package org.kodein.memory.crypto.lib.sha_js

import org.kodein.memory.crypto.lib.JsDigest


@JsModule("sha.js")
@JsNonModule
internal external fun shajs(algo: String): JsDigest
