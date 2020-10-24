package io.github.edsuns.adfilter

import io.github.edsuns.adblockclient.AdBlockClient
import timber.log.Timber

/**
 * Created by Edsuns@qq.com on 2020/10/24.
 */
class FilterDataLoader constructor(
    private val adDetector: AdDetector,
    private val filterDataStore: FilterDataStore
) {

    fun load(id: String) {
        if (filterDataStore.hasData(id)) {
            val client = AdBlockClient(id)
            client.loadProcessedData(filterDataStore.loadData(id))
            adDetector.addClient(client)
        } else {
            Timber.v("Couldn't find client processed data: $id")
        }
    }
}