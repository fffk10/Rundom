package com.example.memomap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.memomap.databinding.ActivityAddBinding
import com.example.memomap.databinding.ActivityMainBinding
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

class AddActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var binding: ActivityAddBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        realm = Realm.getDefaultInstance()

        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)

        binding.saveBtn.setOnClickListener {
            val memoStr = binding.memoEdit.text?.toString()?: ""
            realm.executeTransaction {
                val maxId = realm.where<Memo>().max("id")
                val nextId = (maxId?.toLong() ?: 0L) + 1L
                val memo = realm.createObject<Memo>(nextId)

                memo.dateTime = Date()
                memo.lat = lat
                memo.lng = lng
                memo.memo = memoStr
            }
            showToast("保存しました")
            finish()
        }

        binding.cancelBtn.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


    private fun showToast(msg: String) {
        val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
        toast.show()
    }
}