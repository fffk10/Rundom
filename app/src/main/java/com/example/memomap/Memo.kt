package com.example.memomap

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Memo: RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var dateTime: Date = Date()
    var lat: Double = 0.0
    var lng: Double = 0.0
    var memo: String = ""
}