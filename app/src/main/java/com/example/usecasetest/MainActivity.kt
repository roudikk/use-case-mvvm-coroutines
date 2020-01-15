package com.example.usecasetest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.upload().observe(this, Observer {
            progressBar.progress = it.progress
            appendTextView("Progress: ${it.progress}, Result: ${it.result}")
        })

        viewModel.viewState().observe(this, Observer {
            appendTextView(
                when (it) {
                    is MainViewModel.ViewState.Loading -> {
                        textView.text = null
                        "Loading.."
                    }
                    is MainViewModel.ViewState.Error -> "Error: ${it.exception}"
                    is MainViewModel.ViewState.Cancelled -> "Cancelled!"
                    is MainViewModel.ViewState.Success -> "Completed!"
                }
            )
        })

        viewModel.backgroundTask().observe(this, Observer {
            Toast.makeText(this, R.string.background_task_finished, Toast.LENGTH_LONG).show()
        })

        startOver.setOnClickListener { viewModel.loadData(true) }
        cancel.setOnClickListener { viewModel.cancel() }
        error.setOnClickListener { viewModel.throwError() }
    }

    private fun appendTextView(text: String) {
        textView.append("$text\n")
    }
}