package com.lessup.medledger.db

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DatabaseDriverFactory): MedLedgerDatabase {
    val driver = driverFactory.createDriver()
    return MedLedgerDatabase(driver)
}
