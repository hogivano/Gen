package id.trydev.gen.model

import com.google.firebase.firestore.GeoPoint

data class Wilayah(
    private var wilayah: Int?= 0,
    private var location: GeoPoint?= null,
    private var pelanggan:Array<Lokasi>?= arrayOf()
)