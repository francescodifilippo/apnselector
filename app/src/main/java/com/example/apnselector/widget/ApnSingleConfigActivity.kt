package com.example.apnselector.widget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.apnselector.R
import com.example.apnselector.data.ApnInfo
import com.example.apnselector.data.ApnRepository

class ApnSingleConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedApnId = -1
    private var selectedApnName = ""
    private var selectedColor = 0
    private var apnList: List<ApnInfo> = emptyList()
    private lateinit var paletteColors: IntArray

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val SELECTION_HIGHLIGHT = 0x1A1976D2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.activity_apn_single_config)

        paletteColors = intArrayOf(
            getColor(R.color.palette_blue),
            getColor(R.color.palette_red),
            getColor(R.color.palette_green),
            getColor(R.color.palette_orange),
            getColor(R.color.palette_purple),
            getColor(R.color.palette_teal),
            getColor(R.color.palette_pink),
            getColor(R.color.palette_grey)
        )
        selectedColor = paletteColors[0]

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setupColorGrid()

        findViewById<Button>(R.id.btn_cancel).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_save).setOnClickListener { saveAndFinish() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadApnList()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            loadApnList()
        }
    }

    private fun loadApnList() {
        apnList = ApnRepository.getApnList(this)
        val listView = findViewById<ListView>(R.id.config_apn_list)
        val noApnView = findViewById<TextView>(R.id.config_no_apn)

        if (apnList.isEmpty()) {
            listView.visibility = View.GONE
            noApnView.visibility = View.VISIBLE
            return
        }

        noApnView.visibility = View.GONE
        listView.visibility = View.VISIBLE

        val adapter = object : ArrayAdapter<ApnInfo>(
            this, android.R.layout.simple_list_item_2, android.R.id.text1, apnList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val apn = apnList[position]
                view.findViewById<TextView>(android.R.id.text1).text = apn.name
                view.findViewById<TextView>(android.R.id.text2).text = apn.apn
                return view
            }
        }
        listView.adapter = adapter

        listView.setOnItemClickListener { _, view, position, _ ->
            val apn = apnList[position]
            selectedApnId = apn.id
            selectedApnName = apn.name
            for (i in 0 until listView.childCount) {
                listView.getChildAt(i)?.setBackgroundColor(Color.TRANSPARENT)
            }
            view.setBackgroundColor(SELECTION_HIGHLIGHT)
        }

        val activeApn = apnList.firstOrNull { it.isActive }
        if (activeApn != null) {
            selectedApnId = activeApn.id
            selectedApnName = activeApn.name
            listView.post {
                listView.setItemChecked(apnList.indexOf(activeApn), true)
            }
        }
    }

    private fun setupColorGrid() {
        val grid = findViewById<GridLayout>(R.id.color_grid)
        val swatchSize = resources.getDimensionPixelSize(R.dimen.color_swatch_size)
        val swatchMargin = resources.getDimensionPixelSize(R.dimen.color_swatch_margin)

        for (color in paletteColors) {
            val swatch = View(this).apply {
                val params = GridLayout.LayoutParams().apply {
                    width = swatchSize
                    height = swatchSize
                    setMargins(swatchMargin, swatchMargin, swatchMargin, swatchMargin)
                }
                layoutParams = params

                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 8f * resources.displayMetrics.density
                    setColor(color)
                }
                background = drawable

                setOnClickListener {
                    selectedColor = color
                    highlightSelectedColor(grid, this)
                }
            }
            grid.addView(swatch)
        }

        if (grid.childCount > 0) {
            highlightSelectedColor(grid, grid.getChildAt(0))
        }
    }

    private fun highlightSelectedColor(grid: GridLayout, selected: View) {
        val density = resources.displayMetrics.density
        for (i in 0 until grid.childCount) {
            val child = grid.getChildAt(i)
            val bg = child.background as? GradientDrawable
            bg?.setStroke(0, Color.TRANSPARENT)
        }
        val selectedBg = selected.background as? GradientDrawable
        selectedBg?.setStroke((3 * density).toInt(), Color.WHITE)
    }

    private fun saveAndFinish() {
        if (selectedApnId == -1) return

        WidgetPrefsHelper.saveWidgetConfig(this, appWidgetId, selectedApnId, selectedApnName, selectedColor)

        val appWidgetManager = AppWidgetManager.getInstance(this)
        ApnSingleWidgetProvider.updateWidget(this, appWidgetManager, appWidgetId)

        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        })
        finish()
    }
}
