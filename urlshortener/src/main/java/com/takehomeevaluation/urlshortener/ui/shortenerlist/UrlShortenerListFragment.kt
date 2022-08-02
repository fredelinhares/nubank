package com.takehomeevaluation.urlshortener.ui.shortenerlist

import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.takehomeevaluation.core.ViewState
import com.takehomeevaluation.core.baseclasses.BaseFragment
import com.takehomeevaluation.core.extensions.closeKeyBoard
import com.takehomeevaluation.urlshortener.databinding.UrlshortenerListFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class UrlShortenerListFragment :
    BaseFragment<UrlshortenerListFragmentBinding>(UrlshortenerListFragmentBinding::inflate) {

    private val viewModel: UrlShortenerListViewModel by viewModel()

    override fun setupView() {
        setupButtonSendUrl()
        setupUrlEditText()
        setupAdapter()
        setupViewsVisibility(progressBarVisible = false)
    }

    override fun addObservers(owner: LifecycleOwner) {
        viewModel.viewState.observe(owner, {
            when (it) {
                is ViewState.Loading -> setupViewsVisibility(progressBarVisible = true)
                is ViewState.Success -> setupViewsVisibility(progressBarVisible = false)
                is ViewState.Error -> {
                    setupViewsVisibility(progressBarVisible = false)
                }
            }
        })

        viewModel.shortenedUrlResultList.observe(owner, {
            refreshList(it)
        })

        viewModel.buttonSendUrlIsEnable.observe(owner, {
            binding.buttonSendUrl.isEnabled = it
        })
    }

    private fun setupAdapter() {
        with(binding.urlshortenerList) {
            layoutManager = LinearLayoutManager(context)
            adapter = UrlShortenerListAdapter(context)
            addItemDecoration(
                DividerItemDecoration(this.context, RecyclerView.VERTICAL)
            )
        }
    }

    private fun refreshList(urlShortenerViewItemList: List<UrlShortenerItemView>) {
        getUrlShortenerListAdapter().submitItemList(urlShortenerViewItemList)
    }

    private fun getUrlShortenerListAdapter() = binding.urlshortenerList.adapter as UrlShortenerListAdapter

    private fun setupButtonSendUrl() {
        with(binding.buttonSendUrl) {
            isEnabled = false
            setOnClickListener {
                loadData(param = binding.originalUrlEditText.text.toString())
            }
        }
    }

    private fun setupUrlEditText() {
        with(binding.originalUrlEditText) {
            addTextChangedListener {
                viewModel.validateTypedUrl(it.toString())
            }
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) return@setOnFocusChangeListener
                v.closeKeyBoard()
            }
        }
    }

    private fun setupViewsVisibility(progressBarVisible: Boolean) {
        with(binding) {
            progressBar.isVisible = progressBarVisible
            buttonSendUrl.isVisible = !progressBarVisible
            originalUrlEditText.isVisible = !progressBarVisible
            urlshortenerListTitle.isVisible = !progressBarVisible
            binding.urlshortenerScrollView.isVisible = !progressBarVisible
        }
    }

    override fun <String> loadData(param: String) = viewModel.registerUrl(sourceUrl = param)
}
