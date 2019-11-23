package `in`.nakkalites.mediaclient.app.di

import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.app.StethoHelper
import `in`.nakkalites.mediaclient.app.constants.HttpConstants
import `in`.nakkalites.mediaclient.data.user.UserService
import `in`.nakkalites.mediaclient.view.splash.SplashActivity
import `in`.nakkalites.mediaclient.viewmodel.login.LoginVm
import `in`.nakkalites.mediaclient.viewmodel.splash.SplashVm
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val applicationModule = module(override = true) {
    scope(named<SplashActivity>()) {
        scoped { SplashVm() }
    }
}

val viewModelModule = module {
    viewModel { SplashVm() }
    viewModel { LoginVm() }
}

val netModule = module {
    single {
        StethoInterceptor()
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<StethoInterceptor>())
            .build()
    }

    single {
        StethoHelper.injectStethoIfDebug(androidContext(), OkHttpClient.Builder())
            .connectTimeout(HttpConstants.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(HttpConstants.TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(HttpConstants.TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl(
                if (BuildConfig.DEBUG) {
                    HttpConstants.BASE_URL_DEV
                } else {
                    HttpConstants.BASE_URL_PROD
                }
            )
            .callFactory(get<OkHttpClient>())
            .addConverterFactory(MoshiConverterFactory.create(get<Moshi>()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(get<OkHttpClient>())
            .build()
    }

    single { get<Retrofit>().create(UserService::class.java) }
}
