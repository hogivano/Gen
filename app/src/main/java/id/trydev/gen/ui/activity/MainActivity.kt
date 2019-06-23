package id.trydev.gen.ui.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.GeoPoint
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import id.trydev.gen.R
import id.trydev.gen.contract.HomeContract
import id.trydev.gen.presenter.HomePresenter
import kotlinx.android.synthetic.main.activity_main.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.Source
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import id.trydev.gen.ui.adapter.DetailAdapter
import kotlinx.android.synthetic.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), HomeContract.View, PermissionsListener,
        OnMapReadyCallback, LocationEngine, Callback<DirectionsResponse> {

    lateinit var presenter: HomePresenter
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var start: LatLng = LatLng(-7.7326582,110.3965508)
    private lateinit var fromPosition: Map<String, Any>
    private lateinit var toPosition: Map<String, Any>
    private var fromPos : Int = 0
    private var toPos : Int = 1
    private lateinit var listLocation : ArrayList<Map<String, Any>>

    private var ROUTE_LAYER_ID : String = "route-layer-id"
    private var ROUTE_SOURCE_ID: String = "route-source-id"
    private var ICON_LAYER_ID : String = "icon-layer-id"
    private var ICON_SOURCE_ID : String = "icon-source-id"
    private var RED_PIN_ICON_ID : String = "red-pin-icon-id"

    private lateinit var currentRoute : DirectionsRoute
    private lateinit var client : MapboxDirections
    private lateinit var origin : Point
    private lateinit var destination : Point
    private lateinit var navigationMapRoute: NavigationMapRoute
    private lateinit var navigationRoute: NavigationRoute
    private lateinit var navigation: MapboxNavigation
    private lateinit var styles: Style
    private lateinit var textFrom : TextView
    private lateinit var textTo : TextView
    private lateinit var textDistance : TextView
    private lateinit var textClient : TextView
    private lateinit var textDetail : TextView
    private lateinit var btnNext : Button
    private lateinit var btnPrev : Button
    private lateinit var constrain : ConstraintLayout
    private lateinit var constrainDetail : ConstraintLayout
    private lateinit var rv: RecyclerView
    private lateinit var adapter: DetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(
            applicationContext,
            "sk.eyJ1IjoiaG9naXZhbm8iLCJhIjoiY2p2eGJuNmJsMDM2ZTQ5cnAzaG9mYjBvciJ9.HOlhbXIasra9WxPbHvq4Gg"
        )
        setContentView(R.layout.activity_main)

        textFrom = from
        textTo = to
        textDistance = distance
        textClient = person
        textDetail = btnDetail
        btnNext = next
        btnPrev = prev
        constrain = showDetail
        rv = rvDetail
        constrainDetail = divDetail

        this.mapView = idMapView
        mapView?.onCreate(savedInstanceState)

        presenter = HomePresenter()
        presenter.attachView(this)

        mapView?.getMapAsync(OnMapReadyCallback { mapboxMap ->
            this.mapboxMap = mapboxMap
            presenter.getMarker()
        })

        next.setOnClickListener {
            if (toPos < listLocation.size - 1){
                fromPosition = listLocation.get(++fromPos)
                toPosition = listLocation.get(++toPos)
                setOrigin(fromPosition)
                setDestination(toPosition)
                getRoute(styles, origin, destination)
            } else {
                Toast.makeText(applicationContext, "Rute sudah selesai", Toast.LENGTH_SHORT).show()
            }
        }

        prev.setOnClickListener {
            if (fromPos > 0){
                fromPosition = listLocation.get(--fromPos)
                toPosition = listLocation.get(--toPos)
                setOrigin(fromPosition)
                setDestination(toPosition)
                getRoute(styles, origin, destination)
            } else {
                Toast.makeText(applicationContext, "Posisi masih berada di fidi agency", Toast.LENGTH_SHORT).show()
            }
        }

        textDetail.setOnClickListener {
            if (constrainDetail.visibility == View.GONE){
                constrainDetail.visibility = View.VISIBLE
                constrain.visibility = View.GONE
            }
        }

        back.setOnClickListener {
            constrain.visibility = View.VISIBLE
            constrainDetail.visibility = View.GONE
        }
    }

    fun setOrigin(map: Map<String, Any>){
        var geo = map.get("location")
        if (geo.toString() != "null" && geo.toString() != ""){
            geo as GeoPoint
            origin = Point.fromLngLat(geo.longitude, geo.latitude)
        }
    }

    fun setDestination(map: Map<String, Any>){
        var geo = map.get("location")
        if (geo.toString() != "null" && geo.toString() != ""){
            geo as GeoPoint
            destination = Point.fromLngLat(geo.longitude, geo.latitude)
        }
    }

    private fun initSource(loadedMapStyle: Style){
        loadedMapStyle.addSource(GeoJsonSource(ROUTE_SOURCE_ID, FeatureCollection.fromFeatures(arrayOf())))

        val iconGeoJsonSource = GeoJsonSource(
            ICON_SOURCE_ID,
            FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            origin.longitude(),
                            origin.latitude()
                        )
                    ), Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))
                )
            )
        )

        loadedMapStyle.addSource(iconGeoJsonSource)
    }

    private fun initLayers(loadedMapStyle: Style){
        var routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
        routeLayer.setProperties(
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND),
            lineWidth(5f),
            lineColor(Color.parseColor("#009688"))
        )

        loadedMapStyle.addLayer(routeLayer)
    }

    private fun getRoute(loadedMapStyle: Style, origin: Point, destionation: Point){
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken("sk.eyJ1IjoiaG9naXZhbm8iLCJhIjoiY2p2eGJuNmJsMDM2ZTQ5cnAzaG9mYjBvciJ9.HOlhbXIasra9WxPbHvq4Gg")
            .build()
        client.enqueueCall(this)
    }

    override fun loadData(arr: ArrayList<Map<String, Any>>) {
        listLocation = arr

        this.mapboxMap.setStyle(Style.MAPBOX_STREETS, object : Style.OnStyleLoaded {
            @SuppressLint("WrongConstant")
            override fun onStyleLoaded(style: Style) {
                styles = style

                for(document in arr){
                    var geo = document.get("location")
                    if (geo.toString() != "null" && geo.toString() != ""){
                        geo as GeoPoint
                        mapboxMap.addMarker(
                            MarkerOptions()
                                .setPosition(LatLng(geo.latitude, geo.longitude))
                                .setTitle(document.get("Wilayah").toString())
                        )
                    }
                }

                fromPosition = listLocation.get(0)
                toPosition = listLocation.get(1)

                adapter = DetailAdapter(toPosition.get("pelanggan") as ArrayList<Map<String, Any>>, applicationContext)
                rv.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                rv.adapter = adapter

                setOrigin(fromPosition)
                setDestination(toPosition)

                initSource(style)
                initLayers(style)

                getRoute(style, origin, destination)
            }
        })
    }

    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
        Toast.makeText(applicationContext, "Error " + t.message, Toast.LENGTH_SHORT).show()
    }

    override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
        var body = response.body()
        if (response.body() == null){
            Toast.makeText(applicationContext, "Token map salah", Toast.LENGTH_SHORT).show()
        } else if (body!!.routes().size < 1){
            Toast.makeText(applicationContext, "Tidak ditemukan jalur", Toast.LENGTH_SHORT).show()
        }

        currentRoute = body!!.routes().get(0)

        if (styles.isFullyLoaded){
            var source : GeoJsonSource? = styles?.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
            if (source != null){
                distance.text = (currentRoute.distance()?.toString()) + " M"
                textClient.text = (toPosition.get("pelanggan") as ArrayList<Map<String, Any>>).size.toString() + " Pelanggan"
                textFrom.text = fromPosition.get("Wilayah").toString()
                textTo.text = toPosition.get("Wilayah").toString()

                adapter.changeList(toPosition.get("pelanggan") as ArrayList<Map<String, Any>>)

                source.setGeoJson(FeatureCollection.fromFeature(
                    Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry()!!, PRECISION_6))
                ))

                if (constrain.visibility == View.GONE){
                    constrain.visibility = View.VISIBLE
                }
                Log.e("berhasil :" , "fully")
            }
        }

    }

    override fun showError(msg: String, errorCode: Int) {
        Log.e("error in home fragment ", msg + "" + errorCode.toString())
    }

    override fun showLoading() {
        Log.e("show loading", "showed")
    }

    override fun dismissLoading() {
    }

    override fun removeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) {

    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent?) {
    }

    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        callback: LocationEngineCallback<LocationEngineResult>,
        looper: Looper?
    ) {
    }

    override fun requestLocationUpdates(request: LocationEngineRequest, pendingIntent: PendingIntent?) {
    }

    override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult>) {
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {

    }

    override fun onPermissionResult(granted: Boolean) {
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
    }


    override fun onResume() {
        super.onResume()
        presenter = HomePresenter()
        presenter.attachView(this)

        this.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        this.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        this.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        this.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        this.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
