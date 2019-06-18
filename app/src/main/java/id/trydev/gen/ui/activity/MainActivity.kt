package id.trydev.gen.ui.activity

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
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
import com.mapbox.mapboxsdk.utils.BitmapUtils


class MainActivity : AppCompatActivity(), HomeContract.View {
    lateinit var presenter: HomePresenter
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var start: LatLng = LatLng(-7.7326582,110.3965508)

    private var ROUTE_LAYER_ID : String = "route-layer-id"
    private var ROUTE_SOURCE_ID: String = "route-source-id"
    private var ICON_LAYER_ID : String = "icon-layer-id"
    private var ICON_SOURCE_ID : String = "icon-source-id"
    private var RED_PIN_ICON_ID : String = "red-pin-icon-id"

    private lateinit var currentRoute : DirectionsRoute
    private lateinit var client : MapboxDirections
    private lateinit var origin : Point
    private lateinit var destination : Point

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(
            applicationContext,
            "sk.eyJ1IjoiaG9naXZhbm8iLCJhIjoiY2p2eGJuNmJsMDM2ZTQ5cnAzaG9mYjBvciJ9.HOlhbXIasra9WxPbHvq4Gg"
        )
        setContentView(R.layout.activity_main)

        this.mapView = idMapView
        mapView?.onCreate(savedInstanceState)

        presenter = HomePresenter()
        presenter.attachView(this)

        mapView?.getMapAsync(OnMapReadyCallback { mapboxMap ->
            this.mapboxMap = mapboxMap
            this.mapboxMap.setStyle(Style.MAPBOX_STREETS, object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    val iconF = IconFactory.getInstance(applicationContext)
                    mapboxMap.addMarker(
                        MarkerOptions()
                            .setPosition(start)
                            .setIcon(iconF.fromResource(R.drawable.mapbox_mylocation_icon_bearing)
                            )
                    )

                    origin = Point.fromLngLat(110.3965508, -7.7326582)
                    destination = Point.fromLngLat(110.436627, -7.73344)

                    initSource(style)
                    initLayers(style)
                }
            })
        })
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

        var b : Bitmap? = BitmapUtils.getBitmapFromDrawable(
            resources.getDrawable(R.drawable.ic_add_location_black_24dp))

        loadedMapStyle.addImage(RED_PIN_ICON_ID, b!!)
        loadedMapStyle.addLayer(
            SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconIgnorePlacement(true),
                iconOffset(arrayOf(0f, -4f))
            )
        )
    }

    private fun getRoute(loadedMapStyle: Style){
    
    }

    override fun loadData(arr: ArrayList<Map<String, Any>>) {
        for(document in arr){
            var geo = document.get("location")
            if (geo.toString() != "null" && geo.toString() != ""){
                geo as GeoPoint
                mapboxMap.addMarker(
                    MarkerOptions()
                        .setPosition(LatLng(geo.latitude, geo.longitude))
                        .setTitle(document.get("Wilayah").toString())
                )
                Log.e("texting", geo.latitude.toString())
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

    override fun onResume() {
        super.onResume()
        presenter = HomePresenter()
        presenter.attachView(this)
        presenter.getMarker()

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
