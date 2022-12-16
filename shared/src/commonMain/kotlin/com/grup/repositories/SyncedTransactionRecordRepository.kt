package com.grup.repositories

import com.grup.repositories.abstract.RealmTransactionRecordRepository
import io.realm.kotlin.Realm
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class SyncedTransactionRecordRepository
    : RealmTransactionRecordRepository(), KoinComponent {
    override val realm: Realm by inject()
}