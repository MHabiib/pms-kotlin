package com.future.pms.ui.superadmin.loginsuperadmin

import com.future.pms.di.base.BasePresenter
import com.future.pms.model.oauth.Token
import com.future.pms.network.APICreator
import com.future.pms.network.ApiServiceInterface
import com.future.pms.network.AuthAPI
import com.future.pms.network.NetworkConstant.GRANT_TYPE
import com.future.pms.network.RetrofitClient
import com.future.pms.util.Authentication
import com.future.pms.util.Constants.Companion.ROLE_SUPER_ADMIN
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LoginPresenterSuperAdmin @Inject constructor() : BasePresenter<LoginContractSuperAdmin>() {
  private val subscriptions = CompositeDisposable()
  private val api: ApiServiceInterface = RetrofitClient.create()

  fun subscribe() {}

  fun attach(view: LoginContractSuperAdmin) {
    this.view = view
  }

  fun login(username: String, password: String) {
    val authFetcher = APICreator(AuthAPI::class.java).generate()
    val subscribe = authFetcher.auth(username, password, GRANT_TYPE).subscribeOn(
        Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ token: Token ->
      getContext()?.let { Authentication.save(it, token, ROLE_SUPER_ADMIN) }
      view?.let { view -> call(view, view::onSuccess) }
    }, { view?.onError() })
    subscriptions.add(subscribe)
  }

  fun isSuperAdmin(accessToken: String) {
    val subscribe = api.isSuperAdmin(accessToken).subscribeOn(Schedulers.io()).observeOn(
        AndroidSchedulers.mainThread()).subscribe({
      if (null != it) {
        view?.onAuthorized()
      }
    }, {
      getContext()?.let { Authentication.delete(it) }
      view?.onError()
    })
    subscriptions.add(subscribe)
  }
}