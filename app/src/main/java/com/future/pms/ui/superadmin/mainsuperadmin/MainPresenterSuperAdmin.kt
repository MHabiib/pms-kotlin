package com.future.pms.ui.superadmin.mainsuperadmin

import javax.inject.Inject

class MainPresenterSuperAdmin @Inject constructor() {
  private lateinit var view: MainContractSuperAdmin

  fun subscribe() {
    //No implement required
  }

  fun onHomeIconClick() {
    view.showHomeFragment()
  }

  fun attach(view: MainContractSuperAdmin) {
    this.view = view
    view.showHomeFragment()
  }

  fun onActivityClick() {
    view.showListActivityFragment()
  }

  fun onCustomerClick() {
    view.showListCustomerFragment()
  }

  fun onParkingZoneClick() {
    view.showListParkingZoneFragment()
  }
}