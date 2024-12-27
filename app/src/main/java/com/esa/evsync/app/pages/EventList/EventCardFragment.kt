package com.esa.evsync.app.pages.EventList

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.esa.evsync.R
import com.esa.evsync.app.dataModels.EventModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * A fragment representing a list of Items.
 */
class EventCardFragment : Fragment() {

    private var columnCount = 1
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_list, container, false)


        view.findViewById<ImageView>(R.id.btnAddEvent).setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_eventsFragment_to_eventAddFragment)
        }

        var recycleView = view.findViewById<RecyclerView>(R.id.list)
        // Set the adapter
        if (recycleView is RecyclerView) {
            with(recycleView) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Log.d("Firebase", "data request sents")
                        val events = db.collection("events")
                            .whereArrayContains(
                                "members",
                                FirebaseAuth.getInstance().currentUser!!.uid
                            )
                            .get()
                            .await()
                        val eventList = ArrayList<EventModel>()
                        for (event in events.documents) {
                            var eventData = event.toObject(EventModel::class.java)!!
                            eventData.id = event.id
                            eventList.add(eventData)
                        }

                        Log.d("Firebase", "event data fetched: ${eventList}")
                        withContext(Dispatchers.Main) {
                            adapter = EventCardRecyclerViewAdapter(eventList, requireView())
                        }
                    }catch (e: Error) {
                        Log.e("Firebase", "failed to load event list", e)
                    }
                }

            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "1"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            EventCardFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}