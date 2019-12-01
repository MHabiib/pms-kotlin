package com.future.pms.ui.bookingdetail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.future.pms.R
import com.future.pms.databinding.ActivityMainBinding
import com.future.pms.databinding.FragmentBookingDetailBinding
import com.future.pms.di.component.DaggerFragmentComponent
import com.future.pms.di.module.FragmentModule
import com.future.pms.model.customerbooking.CustomerBooking
import com.future.pms.model.oauth.Token
import com.future.pms.ui.main.MainActivity
import com.future.pms.util.Constants.Companion.AUTHENTCATION
import com.future.pms.util.Constants.Companion.BOOKING_DETAIL_FRAGMENT
import com.future.pms.util.Constants.Companion.ERROR
import com.future.pms.util.Constants.Companion.ID_BOOKING
import com.future.pms.util.Constants.Companion.NULL
import com.future.pms.util.Constants.Companion.STATUS_AVAILABLE
import com.future.pms.util.Constants.Companion.STATUS_BOOKED
import com.future.pms.util.Constants.Companion.STATUS_RESERVED
import com.future.pms.util.Constants.Companion.STATUS_ROAD
import com.future.pms.util.Constants.Companion.TOKEN
import com.future.pms.util.Constants.Companion.parkGaping
import com.future.pms.util.Constants.Companion.parkSize
import com.future.pms.util.Constants.Companion.selectedIds
import com.future.pms.util.Utils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_bottom_sheet_content.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class BookingDetailFragment : Fragment(), BookingDetailContract {
  private var parkViewList: MutableList<TextView> = ArrayList()
  @Inject lateinit var presenter: BookingDetailPresenter
  private lateinit var idBooking: String
  private lateinit var accessToken: String
  private lateinit var layout: HorizontalScrollView
  private lateinit var binding: FragmentBookingDetailBinding
  private lateinit var bindingActivityMain: ActivityMainBinding
  private var SLOTS =
    ("/\$_UUAAU_RR_UU_UU_/" + "________________/" + "_AARAU_UU_UU_UU_/" + "_UUARR_RR_UU_AR_/" + "________________/" + "_URAAU_RA_UU_UU_/" + "_RUUAU_RR_UU_UU_/" + "________________/" + "_UU_AU_RU_UR_UU_/" + "_UU_AU_RR_AR_UU_/" + "________________/" + "_UURAUARRAUUAUU_/" + "________________/" + "_URRAUARARUURUU_/" + "________________/")

  companion object {
    const val TAG: String = BOOKING_DETAIL_FRAGMENT
  }

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

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_booking_detail, container, false)
    bindingActivityMain = DataBindingUtil.inflate(inflater, R.layout.activity_main, null, false)

    accessToken = Gson().fromJson(
      context?.getSharedPreferences(AUTHENTCATION, Context.MODE_PRIVATE)?.getString(
        TOKEN, null
      ), Token::class.java
    ).accessToken
    binding.parkingDirectionContent.backBookingDetail.setOnClickListener { backToHome() }
    binding.parkingDirectionContent.buttonScanAgain.setOnClickListener { scanAgain() }
    idBooking = this.arguments?.getString(ID_BOOKING).toString()
    layout = binding.parkingDirectionSheet.layoutPark.findViewById(R.id.layoutPark)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.attach(this)
    presenter.subscribe()
    if (NULL != idBooking) {
      presenter.loadBooking(accessToken)
    } else {
      showProgress(false)
      binding.parkingDirectionContent.errorText.visibility = View.VISIBLE
      binding.parkingDirectionContent.buttonScanAgain.visibility = View.VISIBLE
      binding.parkingDirectionContent.iconBookingDetail.setImageResource(R.drawable.ic_sad)
      binding.parkingDirectionContent.welcomeTo.text = getString(R.string.oops)
      binding.parkingDirectionContent.errorText.text = getString(R.string.failed_create_booking)
      binding.parkingDirectionSheet.root.visibility = View.GONE
    }
  }

  override fun loadBookingSuccess(booking: CustomerBooking) {
    showParkingLayout(layout)
    binding.parkingDirectionSheet.swipeUpIndicator.visibility = View.VISIBLE
    binding.parkingDirectionContent.welcomeTo.text =
      String.format(getString(R.string.welcome_to), booking.parkingZoneName)
    binding.parkingDirectionContent.slotName.text = booking.slotName
    binding.parkingDirectionContent.layoutBookingDetail.visibility = View.VISIBLE
    binding.parkingDirectionContent.iconBookingDetail.setImageResource(R.drawable.ic_smile)
    binding.parkingDirectionContent.dateIn.text = Utils.convertLongToTimeOnly(booking.dateIn)
    hideItem()
  }

  private fun hideItem() {
    val navigationView = bindingActivityMain.navView
    navigationView.menu.findItem(R.id.navigation_home).isVisible = false
  }

  private fun backToHome() {
    val activity = activity as MainActivity?
    activity?.presenter?.onHomeIconClick()
  }

  private fun scanAgain() {
    val activity = activity as MainActivity?
    activity?.presenter?.onScanIconClick()
  }

  override fun showProgress(show: Boolean) {
    if (null != progressBar && show) {
      progressBar.visibility = View.VISIBLE
    } else if (null != progressBar && !show) {
      progressBar.visibility = View.GONE
    }
  }

  override fun showErrorMessage(error: String) {
    Timber.tag(ERROR).e(error)
  }

  private fun injectDependency() {
    val homeComponent = DaggerFragmentComponent.builder().fragmentModule(FragmentModule()).build()
    homeComponent.inject(this)
  }

  private fun showParkingLayout(layout: HorizontalScrollView) {
    val layoutPark = LinearLayout(context)
    var parkingLayout: LinearLayout? = null
    var count = 0
    val params = LinearLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    layoutPark.orientation = LinearLayout.VERTICAL
    layoutPark.layoutParams = params
    layoutPark.setPadding(4 * parkGaping, 4 * parkGaping, 4 * parkGaping, 4 * parkGaping)
    layout.addView(layoutPark)

    for (index in 0 until SLOTS.length) {
      when {
        SLOTS[index] == '/' -> {
          parkingLayout = LinearLayout(context)
          parkingLayout.orientation = LinearLayout.HORIZONTAL
          layoutPark.addView(parkingLayout)
        }
        SLOTS[index] == 'U' -> {
          count++
          setupParkingView(count, parkingLayout, SLOTS[index], STATUS_BOOKED, R.drawable.ic_car)
        }
        SLOTS[index] == 'A' -> {
          count++
          setupParkingView(count, parkingLayout, SLOTS[index], STATUS_AVAILABLE, R.drawable.ic_park)
        }
        SLOTS[index] == 'R' -> {
          count++
          setupParkingView(
            count, parkingLayout, SLOTS[index], STATUS_RESERVED, R.drawable.ic_disable
          )
        }
        SLOTS[index] == '_' -> {
          setupParkingView(count, parkingLayout, SLOTS[index], STATUS_ROAD, R.drawable.ic_road)
        }
      }
    }
  }

  private fun setupParkingView(
    count: Int, layout: LinearLayout?, code: Char, tag: Int, icon: Int
  ): TextView {
    val view = TextView(context)
    val layoutParams = LinearLayout.LayoutParams(parkSize, parkSize)
    layoutParams.setMargins(
      parkGaping, parkGaping, parkGaping, parkGaping
    )
    view.layoutParams = layoutParams
    view.setPadding(0, 0, 0, 0)
    view.gravity = Gravity.CENTER
    view.setBackgroundResource(icon)
    view.setTextColor(Color.WHITE)
    view.tag = tag
    if (code != '_') {
      view.id = count
      view.text = count.toString()
      view.setOnClickListener { onClick(view) }
    } else {
      view.text = ""
    }
    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9f)
    layout!!.addView(view)
    parkViewList.add(view)
    return view
  }

  private fun onClick(view: View) {
    if (view.tag as Int == STATUS_AVAILABLE) {
      if (selectedIds.contains(view.id.toString() + ",")) {
        selectedIds = selectedIds.replace((+view.id).toString() + ",", "")
        view.setBackgroundResource(R.drawable.ic_park)
      } else {
        selectedIds = selectedIds + view.id + ","
        view.setBackgroundResource(R.drawable.ic_my_location)
      }
    } else if (view.tag as Int == STATUS_BOOKED) {
      Toast.makeText(
        context,
        String.format(getString(R.string.park_is_booked), view.id),
        Toast.LENGTH_SHORT
      ).show()
    } else if (view.tag as Int == STATUS_RESERVED) {
      Toast.makeText(
        context,
        String.format(getString(R.string.park_is_reserved), view.id),
        Toast.LENGTH_SHORT
      ).show()
    }
  }
}