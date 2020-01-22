package com.future.pms.ui.superadmin.listuser.injection

import com.future.pms.ui.superadmin.listuser.network.ListUserApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module class ListUserModule {
  @Provides fun provideListUserApi(retrofit: Retrofit): ListUserApi {
    return retrofit.create(ListUserApi::class.java)
  }
}