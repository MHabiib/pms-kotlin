package com.future.pms.ui.main

interface MainContract {
  fun showHomeFragment()
  fun showScanFragment()
  fun showProfileFragment()
  fun showParkingDirection(idBooking: String, levelName: String)
  fun showBookingDetail(idBooking: String)
  fun showBookingFailed()
  fun showLoginPage()
}