package com.example.memomap

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * MainActivityよりも先に実行されて
 * Realmの準備
 */
class CustomApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        // RealmのDB更新処理をUIスレッド上で行うのを許可
        val config = RealmConfiguration.Builder()
            .allowWritesOnUiThread(true)
            .build()

        Realm.setDefaultConfiguration(config)
    }
}