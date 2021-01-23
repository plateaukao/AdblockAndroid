/*
 * Copyright (c) 2017 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.edsuns.adblockclient

import android.net.Uri
import timber.log.Timber


class AdBlockClient(override val id: String) : Client {

    private val nativeClientPointer: Long
    private var rawDataPointer: Long
    private var processedDataPointer: Long

    init {
        System.loadLibrary("adblock-client")
        nativeClientPointer = createClient()
        rawDataPointer = 0
        processedDataPointer = 0
    }

    private external fun createClient(): Long

    fun loadBasicData(data: ByteArray, preserveRules: Boolean = false) {
        val timestamp = System.currentTimeMillis()
        Timber.d("Loading basic data for $id")
        rawDataPointer = loadBasicData(nativeClientPointer, data, preserveRules)
        Timber.d("Loading basic data for $id completed in ${System.currentTimeMillis() - timestamp}ms")
    }

    fun setGenericElementHidingEnabled(enabled: Boolean) =
        setGenericElementHidingEnabled(nativeClientPointer, enabled)

    private external fun setGenericElementHidingEnabled(clientPointer: Long, enabled: Boolean)

    private external fun loadBasicData(
        clientPointer: Long,
        data: ByteArray,
        preserveRules: Boolean
    ): Long

    fun loadProcessedData(data: ByteArray) {
        val timestamp = System.currentTimeMillis()
        Timber.d("Loading preprocessed data for $id")
        processedDataPointer = loadProcessedData(nativeClientPointer, data)
        Timber.d("Loading preprocessed data for $id completed in ${System.currentTimeMillis() - timestamp}ms")
    }

    private external fun loadProcessedData(clientPointer: Long, data: ByteArray): Long

    fun getProcessedData(): ByteArray = getProcessedData(nativeClientPointer)

    private external fun getProcessedData(clientPointer: Long): ByteArray

    fun getFiltersCount(): Int = getFiltersCount(nativeClientPointer)

    private external fun getFiltersCount(clientPointer: Long): Int

    override fun matches(
        url: String,
        documentUrl: String,
        resourceType: ResourceType
    ): MatchResult {
        val firstPartyDomain = documentUrl.baseHost() ?: return MatchResult(false, null, null)
        return matches(nativeClientPointer, url, firstPartyDomain, resourceType.filterOption)
    }

    private external fun matches(
        clientPointer: Long,
        url: String,
        firstPartyDomain: String,
        filterOption: Int
    ): MatchResult

    override fun getElementHidingSelectors(url: String): String? =
        getElementHidingSelectors(nativeClientPointer, url)

    private external fun getElementHidingSelectors(clientPointer: Long, url: String): String?

    @Suppress("unused", "protectedInFinal")
    protected fun finalize() {
        releaseClient(nativeClientPointer, rawDataPointer, processedDataPointer)
    }

    private external fun releaseClient(
        clientPointer: Long,
        rawDataPointer: Long,
        processedDataPointer: Long
    )

    private fun String.baseHost(): String? {
        return Uri.parse(this).host?.removePrefix("www.")
    }
}
