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
            appendTextView(
                when (it) {
                    is ViewState.Loading -> {
                        progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                            Color.parseColor("#FFFFFF")
                        )
                        textView.text = null
                        "Loading.."
                    }
                    is ViewState.Error -> {
                        progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                            Color.parseColor("#E91E63")
                        )
                        progressBar.progress = 0
                        "Error: ${it.throwable}"
                    }
                    is ViewState.Cancelled -> {
                        progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                            Color.parseColor("#FF9800")
                        )
                        progressBar.progress = 0
                        "Cancelled!"
                    }
                    is ViewState.Success -> {
                        it.result?.apply { progressBar.progress = it.result.progress }
                        "Completed!"
                    }
                    is ViewState.Result -> {
                        progressBar.progress = it.result.progress
                        "Progress: ${it.result.progress}"
                    }
                }
            )
        })

        start.setOnClickListener { viewModel.loadData(true) }
        cancel.setOnClickListener { viewModel.cancel() }
        error.setOnClickListener { viewModel.throwError() }
    }

    private fun appendTextView(text: String) {
        textView.append("$text\n")
    }
}