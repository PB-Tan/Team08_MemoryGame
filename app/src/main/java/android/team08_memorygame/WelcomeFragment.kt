package android.team08_memorygame

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.findNavController

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d("WelcomeFragment", "onCreateView called")

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        val forwardButton1 = view.findViewById<ImageButton>(R.id.forward1)
        forwardButton1.setOnClickListener {
            view.findNavController().navigate(R.id.action_WelcomeFragment_to_GiftFragment)
        }
        return view

    }
            }
