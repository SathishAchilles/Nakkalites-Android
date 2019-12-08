package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.viewmodel.utils.DummyVm
import `in`.nakkalites.mediaclient.viewmodel.utils.ProgressBarVm
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm

/** Common implementations of [ViewProvider]. */
object ViewProviders {
    private const val noLayout = -1

    @JvmStatic
    fun dummyViewProvider(): ViewProvider =
        viewProvider { vm -> (vm as? DummyVm)?.layoutId ?: noLayout }

    @JvmStatic
    fun progressViewProvider(): ViewProvider =
        viewProvider { vm -> if (vm is ProgressBarVm) R.layout.progress_bar else noLayout }

    @JvmStatic
    fun wrapSequentially(vararg viewProviders: ViewProvider): ViewProvider =
        viewProvider { vm ->
            val handler = viewProviders.find { it.getView(vm) != noLayout }
            return@viewProvider handler?.getView(vm) ?: noLayout
        }

    @JvmStatic
    fun videoItemViewProvider(): ViewProvider =
        viewProvider { vm -> if (vm is VideoVm) R.layout.item_video else noLayout }

    @JvmStatic
    fun videoGroupItemViewProvider(): ViewProvider =
        viewProvider { vm -> if (vm is VideoGroupVm) R.layout.item_video_group else noLayout }
}
