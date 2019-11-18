package com.future.pms.ui.bookingdetail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.future.pms.R
import com.future.pms.di.component.DaggerFragmentComponent
import com.future.pms.di.module.FragmentModule
import com.future.pms.model.customerbooking.CustomerBooking
import com.future.pms.model.oauth.Token
import com.future.pms.ui.main.MainActivity
import com.future.pms.util.Constants
import com.future.pms.util.Constants.Companion.ERROR
import com.future.pms.util.Utils
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_booking_detail.view.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet_content.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet_content.view.*
import java.util.*
import javax.inject.Inject

class BookingDetailFragment : Fragment(), BookingDetailContract {
  private lateinit var rootView: View
  private var idBooking: String = ""
  private var accessToken: String = ""
  private var seatViewList: MutableList<TextView> = ArrayList()
  private lateinit var layout: HorizontalScrollView
  @Inject lateinit var presenter: BookingDetailPresenter
  private var SEATS =
    ("/\$_UUAAU_RR_UU_UU_/" + "________________/" + "_AARAU_UU_UU_UU_/" + "_UUARR_RR_UU_AR_/" + "________________/" + "_URAAU_RA_UU_UU_/" + "_RUUAU_RR_UU_UU_/" + "________________/" + "_UU_AU_RU_UR_UU_/" + "_UU_AU_RR_AR_UU_/" + "________________/" + "_UURAUARRAUUAUU_/" + "________________/" + "_URRAUARARUURUU_/" + "________________/")

  fun newInstance(): BookingDetailFragment {
    return BookingDetailFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requireActivity().onBackPressedDispatcher.addCallback(this) {
      val activity = activity as MainActivity?
      activity?.presenter?.onHomeIconClick()
    }
    injectDependency()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    accessToken = Gson().fromJson(
        context?.getSharedPreferences(Constants.AUTHENTCATION, Context.MODE_PRIVATE)?.getString(
            Constants.TOKEN, null), Token::class.java).access_token
    rootView = inflater.inflate(R.layout.fragment_booking_detail, container, false)
    rootView.findViewById<ImageButton>(R.id.back_booking_detail).setOnClickListener { backToHome() }
    rootView.findViewById<Button>(R.id.button_scan_again).setOnClickListener { scanAgain() }
    idBooking = this.arguments?.getString("idBooking").toString()
    layout = rootView.findViewById(R.id.layoutSeat)
    return rootView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.attach(this)
    presenter.subscribe()
    if ("null" != idBooking) {
      presenter.loadBooking(accessToken)
    } else {
      showProgress(false)
      rootView.error_text.visibility = View.VISIBLE
      rootView.button_scan_again.visibility = View.VISIBLE
      rootView.parking_direction_sheet.visibility = View.GONE
      rootView.icon_booking_detail.setImageResource(R.drawable.ic_sad)
      rootView.welcome_to.text = "Oops..."
      rootView.error_text.text = "Failed to create booking, something went wrong."
    }
  }

  override fun loadBookingSuccess(booking: CustomerBooking) {
    showParkingLayout(layout)
    rootView.welcome_to.text = String.format("Welcome to %s", booking.parkingZoneName)
    rootView.slot_name.text = booking.slotName
    rootView.layout_booking_detail.visibility = View.VISIBLE
    rootView.icon_booking_detail.setImageResource(R.drawable.ic_smile)
    rootView.date_in.text = Utils.convertLongToTimeOnly(booking.dateIn)
    hideItem()
  }

  private fun hideItem() {
    val navigationView = rootView.findViewById(R.id.nav_view) as NavigationView
    navigationView.menu.findItem(R.id.navigation_home).isVisible = false
  }

  fun backToHome() {
    val activity = activity as MainActivity?
    activity?.presenter?.onHomeIconClick()
  }

  fun scanAgain() {
    val activity = activity as MainActivity?
    activity?.presenter?.onScanIconClick()
  }

  override fun showProgress(show: Boolean) {
    if (null != progressBar) {
      if (show) {
        progressBar.visibility = View.VISIBLE
      } else {
        progressBar.visibility = View.GONE
      }
    }
  }

  override fun showErrorMessage(error: String) {
    Log.e(ERROR, error)
  }

  private fun injectDependency() {
    val homeComponent = DaggerFragmentComponent.builder().fragmentModule(FragmentModule()).build()
    homeComponent.inject(this)
  }

  companion object {
    const val TAG: String = Constants.BOOKING_DETAIL_FRAGMENT
  }

  fun showParkingLayout(layout: HorizontalScrollView) {
    val layoutSeat = LinearLayout(context)
    val params = LinearLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    layoutSeat.orientation = LinearLayout.VERTICAL
    layoutSeat.layoutParams = params
    layoutSeat.setPadding(
      8 * Constants.seatGaping,
      8 * Constants.seatGaping,
      8 * Constants.seatGaping,
      8 * Constants.seatGaping
    )
    layout.addView(layoutSeat)

    var layout: LinearLayout? = null
    var count = 0

    for (index in 0 until SEATS.length) {
      if (SEATS.get(index) == '/') {
        layout = LinearLayout(context)
        layout.orientation = LinearLayout.HORIZONTAL
        layoutSeat.addView(layout)
      } else if (SEATS.get(index) == 'U') {
        count++
        val view = TextView(context)
        val layoutParams = LinearLayout.LayoutParams(Constants.seatSize, Constants.seatSize)
        layoutParams.setMargins(
          Constants.seatGaping, Constants.seatGaping, Constants.seatGaping, Constants.seatGaping
        )
        view.layoutParams = layoutParams
        view.setPadding(0, 0, 0, 2 * Constants.seatGaping)
        view.id = count
        view.gravity = Gravity.CENTER
        view.setBackgroundResource(R.drawable.ic_car)
        view.setTextColor(Color.WHITE)
        view.tag = Constants.STATUS_BOOKED
        view.text = count.toString() + ""
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9f)
        layout!!.addView(view)
        seatViewList.add(view)
        view.setOnClickListener { onClick(view) }
      } else if (SEATS.get(index) == 'A') {
        count++
        val view = TextView(context)
        val layoutParams = LinearLayout.LayoutParams(Constants.seatSize, Constants.seatSize)
        layoutParams.setMargins(
          Constants.seatGaping, Constants.seatGaping, Constants.seatGaping, Constants.seatGaping
        )
        view.layoutParams = layoutParams
        view.setPadding(0, 0, 0, 2 * Constants.seatGaping)
        view.id = count
        view.gravity = Gravity.CENTER
        view.setBackgroundResource(R.drawable.ic_park)
        view.text = count.toString() + ""
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9f)
        view.setTextColor(Color.BLACK)
        view.tag = Constants.STATUS_AVAILABLE
        layout!!.addView(view)
        seatViewList.add(view)
        view.setOnClickListener { onClick(view) }
      } else if (SEATS.get(index) == 'R') {
        count++
        val view = TextView(context)
        val layoutParams = LinearLayout.LayoutParams(Constants.seatSize, Constants.seatSize)
        layoutParams.setMargins(
          Constants.seatGaping, Constants.seatGaping, Constants.seatGaping, Constants.seatGaping
        )
        view.layoutParams = layoutParams
        view.setPadding(0, 0, 0, 2 * Constants.seatGaping)
        view.id = count
        view.gravity = Gravity.CENTER
        view.setBackgroundResource(R.drawable.ic_disable)
        view.text = count.toString() + ""
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9f)
        view.setTextColor(Color.WHITE)
        view.tag = Constants.STATUS_RESERVED
        layout!!.addView(view)
        seatViewList.add(view)
        view.setOnClickListener { onClick(view) }
      } else if (SEATS.get(index) == '_') {
        val view = TextView(context)
        val layoutParams = LinearLayout.LayoutParams(Constants.seatSize, Constants.seatSize)
        layoutParams.setMargins(
          Constants.seatGaping, Constants.seatGaping, Constants.seatGaping, Constants.seatGaping
        )
        view.layoutParams = layoutParams
        view.setBackgroundResource(R.drawable.ic_road)
        view.text = ""
        layout!!.addView(view)
      }
    }
  }

  private fun onClick(view: View) {
    if (view.tag as Int == Constants.STATUS_AVAILABLE) {
      if (Constants.selectedIds.contains(view.id.toString() + ",")) {
        Constants.selectedIds = Constants.selectedIds.replace((+view.id).toString() + ",", "")
        view.setBackgroundResource(R.drawable.ic_car)
      } else {
        Constants.selectedIds = Constants.selectedIds + view.id + ","
        view.setBackgroundResource(R.drawable.ic_my_location)
      }
    } else if (view.tag as Int == Constants.STATUS_BOOKED) {
      Toast.makeText(context, "Seat " + view.id + " is Booked", Toast.LENGTH_SHORT).show()
    } else if (view.tag as Int == Constants.STATUS_RESERVED) {
      Toast.makeText(context, "Seat " + view.id + " is Reserved", Toast.LENGTH_SHORT).show()
    }
  }
}