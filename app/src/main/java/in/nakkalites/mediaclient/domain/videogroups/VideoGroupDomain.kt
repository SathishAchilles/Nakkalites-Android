package `in`.nakkalites.mediaclient.domain.videogroups

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.data.PrefsConstants
import `in`.nakkalites.mediaclient.data.videogroup.*
import `in`.nakkalites.mediaclient.domain.BaseDomain
import `in`.nakkalites.mediaclient.domain.models.*
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.reactivex.Single
import timber.log.Timber

class VideoGroupDomain(private val videoGroupService: VideoGroupService, val moshi: Moshi) :
    BaseDomain {

    fun getAllVideoGroups(pagingBody: PagingBody): Single<Triple<List<Banner>, List<VideoGroup>, String?>> {
        loge(pagingBody.toMap().toString())
        val json = "{\n" +
                "  \"banners\": [\n" +
                "    {\n" +
                "      \"title\": \"Banner 1\",\n" +
                "      \"type\": \"webseries\",\n" +
                "      \"web_series\": {\n" +
                "        \"title_name\": \"Nakkalites\",\n" +
                "        \"name\": \"Webseries Season1\",\n" +
                "        \"id\": 123,\n" +
                "        \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\",\n" +
                "        \"no_of_episodes\": 10,\n" +
                "        \"description\": \"\",\n" +
                "        \"video_list\": [\n" +
                "          {\n" +
                "            \"id\": 0,\n" +
                "            \"header_name\": \"popular\",\n" +
                "            \"videos\": [\n" +
                "              {\n" +
                "                \"id\": 123,\n" +
                "                \"title_name\": \"Nakkalites\",\n" +
                "                \"title_type\": \"random\",\n" +
                "                \"video_name\": \" Episode 1\",\n" +
                "                \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "                \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\": 123,\n" +
                "                \"title_name\": \"Nakkalites\",\n" +
                "                \"title_type\": \"random\",\n" +
                "                \"video_name\": \" Episode 2\",\n" +
                "                \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "                \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"title\": \"Banner 2\",\n" +
                "      \"type\": \"video\",\n" +
                "      \"video\": {\n" +
                "        \"id\": 123,\n" +
                "        \"title_name\": \"Nakkalites\",\n" +
                "        \"title_type\": \"random\",\n" +
                "        \"video_name\": \"Webseries Season1\",\n" +
                "        \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "        \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"video_list\": [\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"header_name\": \"popular\",\n" +
                "      \"videos\": [\n" +
                "        {\n" +
                "          \"id\": 123,\n" +
                "          \"title_name\": \"Nakkalites\",\n" +
                "          \"title_type\": \"random\",\n" +
                "          \"video_name\": \" Episode 1\",\n" +
                "          \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "          \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 123,\n" +
                "          \"title_name\": \"Nakkalites\",\n" +
                "          \"title_type\": \"random\",\n" +
                "          \"video_name\": \" Episode 2\",\n" +
                "          \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "          \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"header_name\": \"latest\",\n" +
                "      \"videos\": [\n" +
                "        {\n" +
                "          \"id\": 123,\n" +
                "          \"title_name\": \"Nakkalites\",\n" +
                "          \"title_type\": \"random\",\n" +
                "          \"video_name\": \" Season1\",\n" +
                "          \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "          \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 123,\n" +
                "          \"title_name\": \"Nakkalites\",\n" +
                "          \"title_type\": \"random\",\n" +
                "          \"video_name\": \" Season2\",\n" +
                "          \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "          \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"header_name\": \"web-series-1\",\n" +
                "      \"videos\": [\n" +
                "        {\n" +
                "          \"id\": 123,\n" +
                "          \"title_name\": \"Nakkalites\",\n" +
                "          \"title_type\": \"Webseries\",\n" +
                "          \"video_name\": \"Webseries Season1\",\n" +
                "          \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "          \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 123,\n" +
                "          \"title_name\": \"Nakkalites\",\n" +
                "          \"title_type\": \"Webseries\",\n" +
                "          \"video_name\": \"Webseries Season2\",\n" +
                "          \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "          \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"cursor\": \"<Hash Key for current iterable element>\"\n" +
                "}"
        val jsonAdapter = moshi.adapter(VideoGroupResponse::class.java)
        return Single.just(jsonAdapter.fromJson(json))
//        return videoGroupService.getVideoGroups(pagingBody.toMap())
            .map { response ->
                Timber.e(response.toString())
                Triple(
                    response.banners.map { entity -> Banner.map(entity) },
                    response.videoGroups.map { entity -> VideoGroup.map(entity) },
                    response.cursor
                )
            }
    }

    fun getWebSeriesList(pagingBody: PagingBody): Single<Pair<List<WebSeries>, String?>> {
        val json = "{\n" +
                "  \"web_series_list\": [\n" +
                "    {\n" +
                "      \"title_name\": \"Nakkalites\",\n" +
                "      \"name\": \"Webseries Season1\",\n" +
                "      \"id\": 123,\n" +
                "      \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\",\n" +
                "      \"no_of_episodes\": 10,\n" +
                "      \"description\": \"\",\n" +
                "      \"video_list\": [\n" +
                "        {\n" +
                "          \"id\": 0,\n" +
                "          \"header_name\": \"popular\",\n" +
                "          \"videos\": [\n" +
                "            {\n" +
                "              \"id\": 123,\n" +
                "              \"title_name\": \"Nakkalites\",\n" +
                "              \"title_type\": \"random\",\n" +
                "              \"video_name\": \" Episode 1\",\n" +
                "              \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "              \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"id\": 123,\n" +
                "              \"title_name\": \"Nakkalites\",\n" +
                "              \"title_type\": \"random\",\n" +
                "              \"video_name\": \" Episode 2\",\n" +
                "              \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "              \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"title_name\": \"Nakkalites\",\n" +
                "      \"name\": \"Webseries Season2\",\n" +
                "      \"id\": 123,\n" +
                "      \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\",\n" +
                "      \"no_of_episodes\": 10,\n" +
                "      \"description\": \"\",\n" +
                "      \"video_list\": [\n" +
                "        {\n" +
                "          \"id\": 0,\n" +
                "          \"header_name\": \"popular\",\n" +
                "          \"videos\": [\n" +
                "            {\n" +
                "              \"id\": 123,\n" +
                "              \"title_name\": \"Nakkalites\",\n" +
                "              \"title_type\": \"random\",\n" +
                "              \"video_name\": \" Episode 1\",\n" +
                "              \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "              \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"id\": 123,\n" +
                "              \"title_name\": \"Nakkalites\",\n" +
                "              \"title_type\": \"random\",\n" +
                "              \"video_name\": \" Episode 2\",\n" +
                "              \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "              \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"cursor\": \"<Hash Key for current iterable element>\"\n" +
                "}"
        val jsonAdapter = moshi.adapter(WebSeriesListResponse::class.java)
        return Single.just(jsonAdapter.fromJson(json))
//        return videoGroupService.getWebSeriesList(pagingBody.toMap())
            .map { response ->
                Timber.e(response.toString())
                Pair(response.webSeriesList.map { WebSeries.map(it) }, response.cursor)
            }
    }

    fun getVideos(videoGroupId: String, pagingBody: PagingBody): Single<Pair<VideoGroup, String?>> {
        val json = "{\n" +
                "  \"video_group\": {\n" +
                "    \"id\": 0,\n" +
                "    \"header_name\": \"popular\",\n" +
                "    \"videos\": [\n" +
                "      {\n" +
                "        \"id\": 123,\n" +
                "        \"title_name\": \"Nakkalites\",\n" +
                "        \"title_type\": \"random\",\n" +
                "        \"video_name\": \" Episode 1\",\n" +
                "        \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "        \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 123,\n" +
                "        \"title_name\": \"Nakkalites\",\n" +
                "        \"title_type\": \"random\",\n" +
                "        \"video_name\": \" Episode 2\",\n" +
                "        \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "        \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 123,\n" +
                "        \"title_name\": \"Nakkalites\",\n" +
                "        \"title_type\": \"random\",\n" +
                "        \"video_name\": \" Episode 1\",\n" +
                "        \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "        \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"cursor\": \"<Hash Key for current iterable element>\"\n" +
                "}"
        val jsonAdapter = moshi.adapter(VideosResponse::class.java)
        return Single.just(jsonAdapter.fromJson(json))
//        return videoGroupService.getVideosOfVideoGroup(videoGroupId, pagingBody.toMap())
            .map { response ->
                Timber.e(response.toString())
                Pair(VideoGroup.map(response.videoGroup), response.cursor)
            }
    }

    fun getWebSeriesDetail(id: String): Single<WebSeries> {
        val json = "{\n" +
                "  \"web_series\": {\n" +
                "    \"title_name\": \"Nakkalites\",\n" +
                "    \"name\": \"Webseries Season1\",\n" +
                "    \"id\": 123,\n" +
                "    \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\",\n" +
                "    \"no_of_episodes\": 10,\n" +
                "    \"description\": \"The days that we feared then are the days that we cherish in our memories now. Let us go back and live those anxious moments once again! \",\n" +
                "    \"video_list\": [\n" +
                "      {\n" +
                "        \"id\": 0,\n" +
                "        \"header_name\": \"Episodes\",\n" +
                "        \"videos\": [\n" +
                "          {\n" +
                "            \"id\": 123,\n" +
                "            \"title_name\": \"Nakkalites\",\n" +
                "            \"title_type\": \"random\",\n" +
                "            \"video_name\": \" Episode 1\",\n" +
                "            \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "            \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": 123,\n" +
                "            \"title_name\": \"Nakkalites\",\n" +
                "            \"title_type\": \"random\",\n" +
                "            \"video_name\": \" Episode 2\",\n" +
                "            \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "            \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}"
        val jsonAdapter = moshi.adapter(WebSeriesDetailResponse::class.java)
        return Single.just(jsonAdapter.fromJson(json))
//        return videoGroupService.getWebSeriesDetail(id)
            .map { response ->
                Timber.e(response.toString())
                WebSeries.map(response.webSeries)
            }
    }

    fun getVideoDetail(id: String): Single<Video> {
        val json = "{\n" +
                "  \"video_info\": {\n" +
                "    \"id\": 123,\n" +
                "    \"title_name\": \"Nakkalites\",\n" +
                "    \"title_type\": \"random\",\n" +
                "    \"video_name\": \" Episode 1\",\n" +
                "    \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "    \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\",\n" +
                "    \"description\": \"The days that we feared then are the days that we cherish in our memories now. Let us go back and live those anxious moments once again! \",\n" +
                "    \"share_text\": \"Share this text\",\n" +
                "    \"video_list\": [\n" +
                "      {\n" +
                "        \"id\": 0,\n" +
                "        \"header_name\": \"Episodes\",\n" +
                "        \"videos\": [\n" +
                "          {\n" +
                "            \"id\": 123,\n" +
                "            \"title_name\": \"Nakkalites\",\n" +
                "            \"title_type\": \"random\",\n" +
                "            \"video_name\": \" Episode 1\",\n" +
                "            \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "            \"thumbnail_image\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": 123,\n" +
                "            \"title_name\": \"Nakkalites\",\n" +
                "            \"title_type\": \"random\",\n" +
                "            \"video_name\": \" Episode 2\",\n" +
                "            \"url\": \"https://cn2.zuidadianying.com/20171216/ypaJ7651/index.m3u8\",\n" +
                "            \"thumbnail_image\": \"https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}"
        val jsonAdapter = moshi.adapter(VideoDetailResponse::class.java)
        return Single.just(jsonAdapter.fromJson(json))
//        return videoGroupService.getVideoDetail(id)
            .map { response ->
                Timber.e(response.toString())
                Video.map(response.video)
            }
    }
}
