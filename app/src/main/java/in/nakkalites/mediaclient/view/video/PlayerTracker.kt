package `in`.nakkalites.mediaclient.view.video

interface PlayerTracker {
    var duration: Long
    var shouldPauseCurrentVideo: Boolean
    fun trackVideoProgress(totalDuration : Long, timeElapsed: Long)
}
