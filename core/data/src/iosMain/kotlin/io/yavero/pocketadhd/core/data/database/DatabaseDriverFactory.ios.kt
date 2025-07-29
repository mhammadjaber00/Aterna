package io.yavero.pocketadhd.core.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = PocketAdhdDatabase.Schema,
            name = "pocketadhd.db"
        )
    }
}