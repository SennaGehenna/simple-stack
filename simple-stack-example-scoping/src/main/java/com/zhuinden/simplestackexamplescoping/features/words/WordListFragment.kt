package com.zhuinden.simplestackexamplescoping.features.words

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zhuinden.simplestackexamplescoping.R
import com.zhuinden.simplestackexamplescoping.core.navigation.BaseFragment
import com.zhuinden.simplestackexamplescoping.utils.*
import kotlinx.android.synthetic.main.word_list_view.*

/**
 * Created by Zhuinden on 2018.09.17.
 */

class WordListFragment : BaseFragment() {
    interface ActionHandler {
        fun onAddNewWordClicked(wordListFragment: WordListFragment)
    }

    interface DataProvider {
        val wordList: LiveData<List<String>>
    }

    private val actionHandler by lazy { lookup<ActionHandler>() }
    private val dataProvider by lazy { lookup<DataProvider>() }
    private val wordList by lazy { dataProvider.wordList }

    private val controllerEvents by lazy { lookup<WordEventEmitter>() }

    val adapter = WordListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.word_list_view, container, false)

    @Suppress("NAME_SHADOWING")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        buttonGoToAddNewWord.onClick { view ->
            actionHandler.onAddNewWordClicked(this)
        }

        wordList.observe(this /*getViewLifecycleOwner()*/, Observer { words ->
            adapter.updateWords(words!!)
        })

        controllerEvents.observe(this /*getViewLifecycleOwner()*/) { event ->
            when (event) {
                is WordController.Events.NewWordAdded -> showToast("Added ${event.word}")
            }.safe()
        }
    }
}