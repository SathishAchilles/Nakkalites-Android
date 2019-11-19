package `in`.nakkalites.mediaclient.app.di

import `in`.nakkalites.mediaclient.view.splash.SplashActivity
import `in`.nakkalites.mediaclient.viewmodel.splash.SplashVm
import org.koin.core.qualifier.named
import org.koin.dsl.module

val applicationModule = module(override = true) {

    scope(named<SplashActivity>()) {
        scoped { SplashVm() }
    }
}
