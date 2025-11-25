package com.lessup.medledger.repository

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun <T : Any> Query<T>.asFlow(): Flow<Query<T>> = asFlow()

actual fun <T : Any> Flow<Query<T>>.mapToList(): Flow<List<T>> = mapToList(Dispatchers.IO)
