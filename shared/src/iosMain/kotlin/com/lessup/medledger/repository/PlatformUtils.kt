package com.lessup.medledger.repository

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun <T : Any> Query<T>.asFlow(): Flow<Query<T>> = asFlow()

actual fun <T : Any> Flow<Query<T>>.mapToList(): Flow<List<T>> = mapToList(Dispatchers.Default)
