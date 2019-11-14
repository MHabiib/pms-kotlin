package com.future.pms.ui.ongoing

import com.future.pms.model.customerbooking.CustomerBooking

interface OngoingContract {
    fun showProgress(show: Boolean)
    fun showErrorMessage(error: String)
    fun loadCustomerOngoingSuccess(ongoing: CustomerBooking)
}