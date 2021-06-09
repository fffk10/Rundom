package com.example.memomap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.memomap.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.realm.Realm

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var locationCallback: LocationCallback? = null
    private lateinit var realm: Realm
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 画面をスリープにしない
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Realmのインスタンスを取得
        realm = Realm.getDefaultInstance()
    }

    override fun onStart() {
        super.onStart()
        if (::mMap.isInitialized) {
            putsMarkers()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        checkPermission()
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            myLocationEnable()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // 許可を求め、拒否されていた場合
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            // まだ許可を求めていない
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions:
        Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION -> {
                if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可された
                    myLocationEnable()
                } else {
                    showToast("現在位置は表示できません")
                }
            }
        }
    }

    /**
     * リアルタイムで位置情報を受け取るためにリスナーを仕掛ける
     */
    private fun myLocationEnable() {
        // 赤い波線でエラーが表示されてしまうので
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            val locationRequest = LocationRequest().apply {
                // ①LocationRequestオブジェクトに位置情報を受け取るためのパラメータを設定
                interval = 10000    // 最長更新時間
                fastestInterval = 5000    // 最短更新時間
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY    // 最高精度
            }
            // locationCallbacオブジェクトを生成
            locationCallback = object : LocationCallback() {
                // 位置情報を取得した時に呼び出されるメソッド
                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult?.lastLocation != null) {
                        // 位置情報をlastLocationに取得し、LatLng型にしてcurrentLatLngに代入
                        lastLocation = locationResult.lastLocation
                        val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                        // それをmoveCamera()メソッドの引数に指定することによってカメラを動かす
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                        // Lat = 緯度　Lng = 経度
                        binding.textView.text =
                            "Lat:${lastLocation.latitude}, Lng:${lastLocation.longitude}"
                    }
                }
            }
            // requestLocationUpdatesメソッドにlocationRequestとlocationCallback
            // を与えることでリアルタイムに位置情報を取得することができる
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null)
            putsMarkers()
        }
    }

    override fun onPause() {
        super.onPause()
        if (locationCallback != null) {
            // removeLocationUpdatesメソッドで位置情報の更新をやめる
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
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

    private fun putsMarkers() {
        mMap.clear()
    }
}