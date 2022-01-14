package com.example.park5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import android.widget.ArrayAdapter

class Fragment_ReportError: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_error,container,false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dropMenu = view.findViewById<Spinner>(R.id.problemDropDown)
        val items = arrayOf("Error1", "Error2", "Error3")
        val adapter: ArrayAdapter<String>? = activity?.let { ArrayAdapter<String>(it.applicationContext,R.layout.support_simple_spinner_dropdown_item,items) }
        dropMenu.adapter = adapter

    }


}