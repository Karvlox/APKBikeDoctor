package com.example.bikedoctor.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.MetricsResponse
import com.example.bikedoctor.data.repository.FeedbackRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class Metrics : Fragment() {

    private lateinit var totalFormsText: TextView
    private var complaintsChart: PieChart? = null
    private var aspectsChart: PieChart? = null
    private var deliveryStatusChart: PieChart? = null
    private var reasonsPhrasesChart: PieChart? = null
    private var reasonsWordsChart: PieChart? = null
    private lateinit var loadingProgress: ProgressBar
    private lateinit var contentLayout: LinearLayout
    private var complaintsLegend: LinearLayout? = null
    private var aspectsLegend: LinearLayout? = null
    private var deliveryStatusLegend: LinearLayout? = null
    private var reasonsPhrasesLegend: LinearLayout? = null
    private var reasonsWordsLegend: LinearLayout? = null
    private val repository = FeedbackRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_metrics, container, false)

        // Initialize views safely
        totalFormsText = view.findViewById(R.id.total_forms_text) ?: run {
            Log.e("Metrics", "total_forms_text not found")
            TextView(requireContext()).apply { text = "Error" }
        }
        complaintsChart = view.findViewById(R.id.complaints_chart)
        aspectsChart = view.findViewById(R.id.aspects_chart)
        deliveryStatusChart = view.findViewById(R.id.delivery_status_chart)
        reasonsPhrasesChart = view.findViewById(R.id.reasons_phrases_chart)
        reasonsWordsChart = view.findViewById(R.id.reasons_words_chart)
        loadingProgress = view.findViewById(R.id.loading_progress) ?: run {
            Log.e("Metrics", "loading_progress not found")
            ProgressBar(requireContext()).apply { visibility = View.GONE }
        }
        contentLayout = view.findViewById(R.id.content_layout) ?: run {
            Log.e("Metrics", "content_layout not found")
            LinearLayout(requireContext()).apply { visibility = View.GONE }
        }
        complaintsLegend = view.findViewById(R.id.complaints_legend)
        aspectsLegend = view.findViewById(R.id.aspects_legend)
        deliveryStatusLegend = view.findViewById(R.id.delivery_status_legend)
        reasonsPhrasesLegend = view.findViewById(R.id.reasons_phrases_legend)
        reasonsWordsLegend = view.findViewById(R.id.reasons_words_legend)

        // Setup back button
        view.findViewById<ImageView>(R.id.back_buttom)?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Fetch and display metrics
        fetchMetrics()

        return view
    }

    private fun fetchMetrics() {
        // Ensure loading is visible and content is hidden
        loadingProgress.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE

        val feedbackCall = repository.getFeedbackMetrics()
        val reasonsPhrasesCall = repository.getReasonsMetricsByPhrase()
        val reasonsWordsCall = repository.getReasonsMetricsByWord()
        val deliveryCall = repository.getDeliveryMetrics()

        feedbackCall.enqueue(object : Callback<MetricsResponse> {
            override fun onResponse(call: Call<MetricsResponse>, response: Response<MetricsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { metrics ->
                        complaintsChart?.let { chart ->
                            val totalComplaints = (metrics.quejasRepetidas ?: emptyList()).sumBy { it.conteo }
                            setupPieChart(
                                chart,
                                (metrics.quejasRepetidas ?: emptyList()).map { PieEntry((it.conteo.toFloat() / totalComplaints) * 100, "") },
                                totalComplaints,
                                complaintsLegend,
                                (metrics.quejasRepetidas ?: emptyList()).map { it.texto }
                            )
                        }
                        aspectsChart?.let { chart ->
                            val totalAspects = (metrics.aspectosRepetidos ?: emptyList()).sumBy { it.conteo }
                            setupPieChart(
                                chart,
                                (metrics.aspectosRepetidos ?: emptyList()).map { PieEntry((it.conteo.toFloat() / totalAspects) * 100, "") },
                                totalAspects,
                                aspectsLegend,
                                (metrics.aspectosRepetidos ?: emptyList()).map { it.texto }
                            )
                        }
                    } ?: Log.e("Metrics", "Empty response from feedback metrics")
                } else {
                    Log.e("Metrics", "Failed to load feedback metrics: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MetricsResponse>, t: Throwable) {
                Log.e("Metrics", "Error fetching feedback metrics: ${t.message}")
            }
        })

        reasonsPhrasesCall.enqueue(object : Callback<MetricsResponse> {
            override fun onResponse(call: Call<MetricsResponse>, response: Response<MetricsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { metrics ->
                        reasonsPhrasesChart?.let { chart ->
                            val totalReasons = (metrics.motivosRepetidos ?: emptyList()).sumBy { it.conteo }
                            setupPieChart(
                                chart,
                                (metrics.motivosRepetidos ?: emptyList()).map { PieEntry((it.conteo.toFloat() / totalReasons) * 100, "") },
                                totalReasons,
                                reasonsPhrasesLegend,
                                (metrics.motivosRepetidos ?: emptyList()).map { it.texto }
                            )
                        }
                    } ?: Log.e("Metrics", "Empty response from reasons phrases metrics")
                } else {
                    Log.e("Metrics", "Failed to load reasons phrases metrics: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MetricsResponse>, t: Throwable) {
                Log.e("Metrics", "Error fetching reasons phrases metrics: ${t.message}")
            }
        })

        reasonsWordsCall.enqueue(object : Callback<MetricsResponse> {
            override fun onResponse(call: Call<MetricsResponse>, response: Response<MetricsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { metrics ->
                        reasonsWordsChart?.let { chart ->
                            val totalReasons = (metrics.motivosRepetidos ?: emptyList()).sumBy { it.conteo }
                            setupPieChart(
                                chart,
                                (metrics.motivosRepetidos ?: emptyList()).map { PieEntry((it.conteo.toFloat() / totalReasons) * 100, "") },
                                totalReasons,
                                reasonsWordsLegend,
                                (metrics.motivosRepetidos ?: emptyList()).map { it.texto }
                            )
                        }
                    } ?: Log.e("Metrics", "Empty response from reasons words metrics")
                } else {
                    Log.e("Metrics", "Failed to load reasons words metrics: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MetricsResponse>, t: Throwable) {
                Log.e("Metrics", "Error fetching reasons words metrics: ${t.message}")
            }
        })

        deliveryCall.enqueue(object : Callback<MetricsResponse> {
            override fun onResponse(call: Call<MetricsResponse>, response: Response<MetricsResponse>) {
                loadingProgress.visibility = View.GONE
                contentLayout.visibility = View.VISIBLE

                if (response.isSuccessful) {
                    response.body()?.let { metrics ->
                        deliveryStatusChart?.let { chart ->
                            val totalReparaciones = metrics.totalReparaciones
                            val terminadosPercentage = (metrics.terminados.toFloat() / totalReparaciones) * 100
                            val noTerminadosPercentage = (metrics.noTerminados.toFloat() / totalReparaciones) * 100
                            setupPieChart(
                                chart,
                                listOf(
                                    PieEntry(terminadosPercentage, "Terminados"),
                                    PieEntry(noTerminadosPercentage, "No Terminados")
                                ),
                                totalReparaciones,
                                deliveryStatusLegend,
                                listOf("Terminados", "No Terminados")
                            )
                        }
                        totalFormsText.text = getString(R.string.total_forms, response.body()?.totalReparaciones ?: 0)
                    } ?: run {
                        totalFormsText.text = getString(R.string.error_loading_metrics, "Respuesta vacía")
                        Log.e("Metrics", "Empty response from delivery metrics")
                    }
                } else {
                    totalFormsText.text = getString(R.string.error_loading_metrics, "Código: ${response.code()}")
                    Log.e("Metrics", "Failed to load delivery metrics: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MetricsResponse>, t: Throwable) {
                loadingProgress.visibility = View.GONE
                contentLayout.visibility = View.VISIBLE
                totalFormsText.text = getString(R.string.error_loading_metrics, t.message ?: "Desconocido")
                Log.e("Metrics", "Error fetching delivery metrics: ${t.message}")
            }
        })
    }

    private fun setupPieChart(chart: PieChart, entries: List<PieEntry>, total: Int, legendLayout: LinearLayout?, labels: List<String>) {
        if (chart == null) {
            Log.e("Metrics", "Chart is null")
            return
        }
        // Verificar y reasignar datos en lugar de limpiar
        if (chart.data != null && chart.data.dataSet != null && chart.data.dataSet.entryCount > 0) {
            chart.data = PieData() // Reasignar un PieData vacío
        }
        val colors = listOf(Color.parseColor("#42A5F5"), Color.parseColor("#66BB6A"), Color.parseColor("#FFCA28"),
            Color.parseColor("#EF5350"), Color.parseColor("#AB47BC")).take(entries.size)
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            setDrawValues(true)
            sliceSpace = 2f
        }
        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(chart))
            setValueTextSize(12f)
        }
        chart.apply {
            data = pieData
            centerText = "Total: $total"
            setCenterTextSize(16f)
            setHoleRadius(40f)
            setTransparentCircleRadius(45f)
            description.isEnabled = false
            legend.isEnabled = false
            animateY(1000)
            invalidate()
        }

        // Crear leyenda personalizada
        legendLayout?.removeAllViews()
        labels.forEachIndexed { index, label ->
            val textView = TextView(context).apply {
                text = "$label"
                textSize = 12f
                setTextColor(Color.BLACK)
                setPadding(8, 4, 8, 4)
                setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(context, android.R.drawable.btn_star)!!.apply {
                        setTint(colors[index % colors.size])
                    }, null, null, null
                )
                compoundDrawablePadding = 8
            }
            legendLayout?.addView(textView)
        }
    }
}