package com.example.bikedoctor.ui.service

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bikedoctor.R
import com.example.bikedoctor.ui.service.fragments.ReceptionFragment
import com.example.bikedoctor.ui.service.fragments.DiagnosticFragment
import com.example.bikedoctor.ui.service.fragments.SparePartsFragment
import com.example.bikedoctor.ui.service.fragments.CostApprovalFragment
import com.example.bikedoctor.ui.service.fragments.RepairFragment
import com.example.bikedoctor.ui.service.fragments.ControlFragment
import com.example.bikedoctor.ui.service.fragments.DeliveryFragment
import android.widget.ImageView

class TableWorkFragment : Fragment() {

    private val TAG = "TableWorkFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for TableWorkFragment
        val view = inflater.inflate(R.layout.fragment_table_work, container, false)

        // Find the ImageViews
        val recepcionButton: ImageView = view.findViewById(R.id.bottom_recepcion)
        val diagnosticoButton: ImageView = view.findViewById(R.id.bottom_diagnostico)
        val repuestosButton: ImageView = view.findViewById(R.id.bottom_repuestos)
        val aprobacionCostosButton: ImageView = view.findViewById(R.id.bottom_aprobacionCostos)
        val reparacionButton: ImageView = view.findViewById(R.id.bottom_reparacion)
        val controlButton: ImageView = view.findViewById(R.id.bottom_control)
        val entregaButton: ImageView = view.findViewById(R.id.bottom_entrega)

        // Set click listeners for each ImageView with logging
        recepcionButton.setOnClickListener {
            Log.d(TAG, "Reception button clicked")
            loadFragment(ReceptionFragment())
        }
        diagnosticoButton.setOnClickListener {
            Log.d(TAG, "Diagnostic button clicked")
            loadFragment(DiagnosticFragment())
        }
        repuestosButton.setOnClickListener {
            Log.d(TAG, "Spare Parts button clicked")
            loadFragment(SparePartsFragment())
        }
        aprobacionCostosButton.setOnClickListener {
            Log.d(TAG, "Cost Approval button clicked")
            loadFragment(CostApprovalFragment())
        }
        reparacionButton.setOnClickListener {
            Log.d(TAG, "Repair button clicked")
            loadFragment(RepairFragment())
        }
        controlButton.setOnClickListener {
            Log.d(TAG, "Control button clicked")
            loadFragment(ControlFragment())
        }
        entregaButton.setOnClickListener {
            Log.d(TAG, "Delivery button clicked")
            loadFragment(DeliveryFragment())
        }

        // Load the default fragment (optional)
        if (savedInstanceState == null) {
            Log.d(TAG, "Loading default ReceptionFragment")
            loadFragment(ReceptionFragment())
        }

        return view
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            // Perform the fragment transaction
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, fragment)
            transaction.commit()
            Log.d(TAG, "Fragment ${fragment.javaClass.simpleName} loaded")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading fragment: ${e.message}")
        }
    }
}