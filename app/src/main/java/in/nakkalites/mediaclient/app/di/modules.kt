package `in`.nakkalites.mediaclient.app.di

import `in`.nakkalites.mediaclient.app.StethoHelper
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.domain.login.UserDataStore
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.data.user.UserService
import `in`.nakkalites.mediaclient.data.videogroup.VideoGroupService
import `in`.nakkalites.mediaclient.domain.login.LoginDomain
import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.viewmodel.home.AllVideoGroupsVm
import `in`.nakkalites.mediaclient.viewmodel.home.HomeVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesListVm
import `in`.nakkalites.mediaclient.viewmodel.login.LoginVm
import `in`.nakkalites.mediaclient.viewmodel.splash.SplashVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupDetailVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesDetailVm
import android.content.Context
import android.content.SharedPreferences
import android.os.StatFs
import android.preference.PreferenceManager
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

val applicationModule = module {
    single<SharedPreferences> {
        PreferenceManager.getDefaultSharedPreferences(androidContext())
    }
    single {
        UserDataStore(get(), get())
    }
    single {
        UserManager(get(), get())
    }
    single {
        LoginDomain(get())
    }
    single {
        VideoGroupDomain(get(), get())
    }
}

val viewModelModule = module {
    viewModel { SplashVm(get()) }
    viewModel { LoginVm(get()) }
    viewModel { AllVideoGroupsVm(get()) }
    viewModel { WebSeriesListVm(get()) }
    viewModel { HomeVm(get(), get(), get()) }
    viewModel { VideoGroupDetailVm(get()) }
    viewModel { WebSeriesDetailVm(get()) }
}

fun netModule(serverUrl: String) = module {
    single {
        StethoInterceptor()
    }
    single {
        HeadersInterceptor()
    }
    single {
        getOkHttpBuilder(
            androidContext(),
            listOf(get<HeadersInterceptor>(), get<StethoInterceptor>())
        )
    }
    single {
        StethoHelper.injectStethoIfDebug(androidContext(), get())
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
            .baseUrl(serverUrl)
            .addConverterFactory(MoshiConverterFactory.create(get<Moshi>()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(get<OkHttpClient>())
            .build()
    }
    single { get<Retrofit>().create(UserService::class.java) }
    single { get<Retrofit>().create(VideoGroupService::class.java) }
    single {
        val downloader = OkHttp3Downloader(get<OkHttpClient>())
        Picasso.Builder(androidContext()).downloader(downloader).build()
    }
}

private fun getOkHttpBuilder(
    context: Context, interceptors: List<Interceptor>
): OkHttpClient.Builder {
    val okHttpClientBuilder = StethoHelper.injectStethoIfDebug(context, OkHttpClient.Builder())
        .cache(createDefaultCache(context))
        .connectTimeout(HttpConstants.TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(HttpConstants.TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(HttpConstants.TIMEOUT, TimeUnit.SECONDS)
    interceptors.forEach { interceptor -> okHttpClientBuilder.addInterceptor(interceptor) }
    return okHttpClientBuilder
}

private fun createDefaultCache(context: Context): Cache {
    val dir = defaultCacheDir(context)
    return Cache(dir, calculateDiskCacheSize(dir))
}

private fun defaultCacheDir(context: Context): File {
    val cache = File(context.applicationContext.cacheDir, AppConstants.PICASSO_CACHE)
    if (!cache.exists()) {
        cache.mkdirs()
    }
    return cache
}

private fun calculateDiskCacheSize(dir: File): Long {
    var size = AppConstants.MIN_DISK_CACHE_SIZE

    try {
        val statFs = StatFs(dir.absolutePath)
        val available = statFs.blockCountLong * statFs.blockSizeLong
        // Target 2% of the total space.
        size = available / 50
    } catch (ignored: IllegalArgumentException) {
    }

    // Bound inside min/max size for disk cache.
    return max(min(size, AppConstants.MAX_DISK_CACHE_SIZE), AppConstants.MIN_DISK_CACHE_SIZE)
}
