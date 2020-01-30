package com.example.usecasetest

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Use Case Test"

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.viewState().observe(this, Observer {
            when (it) {
                is ViewState.Loading -> {
                    progressBar.progressTintList = ColorStateList.valueOf(
                        Color.parseColor("#03A9F4")
                    )
                    progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#FFFFFF")
                    )
                    textView.text = null
                    appendTextView("Loading..")
                }
                is ViewState.Error -> {
                    progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#E91E63")
                    )
                    progressBar.progress = 0
                    appendTextView("Error: ${it.throwable}")
                }
                is ViewState.Cancelled -> {
                    progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#FF9800")
                    )
                    progressBar.progress = 0
                    appendTextView("Cancelled!")
                }
                is ViewState.Success -> {
                    progressBar.progressTintList = ColorStateList.valueOf(
                        Color.parseColor("#4CAF50")
                    )
                    it.result?.apply { progressBar.progress = it.result.progress }
                    appendTextView("Completed!")
                }
                is ViewState.Result -> {
                    progressBar.progress = it.result.progress
                    appendTextView("Progress: ${it.result.progress}")
                }
            }
        })

        start.setOnClickListener { viewModel.loadData(true) }
        cancel.setOnClickListener { viewModel.cancel() }
        error.setOnClickListener { viewModel.throwError() }
    }

    private fun appendTextView(text: String) {
        textView.append("$text\n")
    }
}