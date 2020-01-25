package com.future.pms.superadmin.loginsuperadmin.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.future.pms.BaseApp
import com.future.pms.R
import com.future.pms.core.base.BaseActivity
import com.future.pms.core.model.Token
import com.future.pms.core.network.Authentication
import com.future.pms.login.view.LoginActivity
import com.future.pms.superadmin.loginsuperadmin.injection.DaggerLoginActivitySuperAdminComponent
import com.future.pms.superadmin.loginsuperadmin.injection.LoginActivitySuperAdminComponent
import com.future.pms.superadmin.loginsuperadmin.presenter.LoginPresenterSuperAdmin
import com.future.pms.superadmin.mainsuperadmin.view.MainActivitySuperAdmin
import com.future.pms.util.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject

class LoginActivitySuperAdmin : BaseActivity(), LoginContractSuperAdmin {
  private var daggerBuild: LoginActivitySuperAdminComponent = DaggerLoginActivitySuperAdminComponent.builder().baseComponent(
      BaseApp.instance.baseComponent).build()

  init {
    daggerBuild.inject(this)
  }

  @Inject lateinit var presenter: LoginPresenterSuperAdmin
  private var count = 0
  private var startMillis: Long = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login_super_admin)
    presenter.attach(this)
    presenter.subscribe()
    btnSign.setOnClickListener {
      if (isValid()) {
        loading(true)
        hideKeyboard()
        presenter.login(txtEmail.text.toString(), txtPassword.text.toString())
      }
    }
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    val eventAction = event.action
    if (eventAction == MotionEvent.ACTION_UP) {
      val time = System.currentTimeMillis()
      if (startMillis == 0L || (time - startMillis > 3000)) {
        startMillis = time
        count = 1
      } else {
        count++
      }
      if (count == 5) {
        Toast.makeText(this, "Switched to CUSTOMER page", Toast.LENGTH_LONG).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
      }
      return true
    }
    return false
  }

  private fun isValid(): Boolean {
    if (txtEmail?.text.toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(
            txtEmail?.text.toString()).matches()) return false
    if (txtPassword?.text.toString().isEmpty()) return false
    return true
  }

  override fun onSuccess(token: Token) {
    Authentication.save(this, token, Constants.ROLE_SUPER_ADMIN)
    presenter.isSuperAdmin(Gson().fromJson(
        this.getSharedPreferences(Constants.AUTHENTICATION, Context.MODE_PRIVATE)?.getString(
            Constants.TOKEN, null), Token::class.java).accessToken)
  }

  override fun onAuthorized() {
    val intent = Intent(this, MainActivitySuperAdmin::class.java)
    startActivity(intent)
    finish()
  }

  private fun hideKeyboard() {
    val view = currentFocus
    view?.let {
      val mInputMethodManager = getSystemService(
          Activity.INPUT_METHOD_SERVICE) as InputMethodManager
      mInputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
  }

  private fun loading(isLoading: Boolean) {
    if (isLoading) {
      inputLayoutEmail.visibility = View.GONE
      progressBar.visibility = View.VISIBLE
      inputLayoutPassword.visibility = View.GONE
      btnSign.visibility = View.GONE
    } else {
      txtPassword.text?.clear()
      progressBar.visibility = View.GONE
      inputLayoutEmail.visibility = View.VISIBLE
      inputLayoutPassword.visibility = View.VISIBLE
      btnSign.visibility = View.VISIBLE
    }
  }

  override fun onFailed(e: String) {
    loading(false)
    Toast.makeText(this, R.string.email_password_incorrect, Toast.LENGTH_LONG).show()
    Authentication.delete(this)
  }

  override fun onBackPressed() {
    super.onBackPressed()
    this.finishAffinity()
  }
}
