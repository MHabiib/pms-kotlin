package com.future.pms.network

import android.content.Context
import com.future.pms.R
import com.future.pms.model.oauth.Token
import com.future.pms.model.oauth.request.Auth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object AuthFetcher {
  class AuthFetcherImpl(
    private val context: Context, private val listener: Listener
  ) {
    private var callback: Call<Token>? = null

    fun auth(auth: Auth) {
      val authFetcher = APICreator(AuthAPI::class.java).generate()
      callback = authFetcher.auth(auth.username, auth.password, auth.grantType)
      callback?.enqueue(object : Callback<Token> {
        override fun onResponse(call: Call<Token>?, response: Response<Token>?) {
          if (null != response && response.isSuccessful) {
            listener.onSuccess(response.body())
          } else if (null != response && !response.isSuccessful) {
            listener.onSuccess(null)
          } else {
            listener.onError(Throwable(context.getString(R.string.auth_error)))
          }
        }

        override fun onFailure(call: Call<Token>?, t: Throwable?) {
          val msg = context.getString(R.string.auth_error)
          listener.onError(Throwable("$msg : ${t?.message}"))
        }
      })
    }
  }

  interface Listener {
    fun onSuccess(token: Token?)
    fun onError(throwable: Throwable)
  }
}