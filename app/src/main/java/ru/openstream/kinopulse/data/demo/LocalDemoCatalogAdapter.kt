package ru.openstream.kinopulse.data.demo

import ru.openstream.kinopulse.domain.model.EpisodeItem
import ru.openstream.kinopulse.domain.model.MediaItem
import ru.openstream.kinopulse.domain.model.MediaType
import ru.openstream.kinopulse.domain.model.SeasonItem
import ru.openstream.kinopulse.domain.model.StreamVariant
import ru.openstream.kinopulse.domain.source.CatalogSourceAdapter
import ru.openstream.kinopulse.domain.source.SourceCapability

/**
 * Локальный демонстрационный адаптер.
 * Работает полностью без бэкенда и ключей, содержит открытые тестовые фильмы (Creative Commons / Public Domain).
 */
class LocalDemoCatalogAdapter : CatalogSourceAdapter {

    override val sourceId: String = "local_demo"
    override val displayName: String = "Локальный демо-каталог (Open Movies)"
    override val isDemoSource: Boolean = true
    override val capabilities: Set<SourceCapability> = setOf(
        SourceCapability.RECENT_RELEASES,
        SourceCapability.SEARCH,
        SourceCapability.DETAILS,
        SourceCapability.SEASONS_AND_EPISODES,
        SourceCapability.DIRECT_STREAM_VARIANTS
    )

    private val demoCatalog: List<MediaItem> = listOf(
        MediaItem(
            id = "demo_sintel",
            title = "Синтел — трейлер",
            originalTitle = "Sintel",
            year = 2010,
            type = MediaType.MOVIE,
            rating = "7.5",
            genres = listOf("Фэнтези", "Приключения", "Анимация"),
            description = "Одинокая девушка Синтел спасает маленького дракона и называет его Скейлз. Когда малыша похищает взрослый дракон, героиня отправляется в опасный путь через земли древних воинов.",
            licenseInfo = "Blender Foundation • CC BY 3.0 • https://creativecommons.org/licenses/by/3.0/",
            posterColorStart = 0xFF3B1D38,
            posterColorEnd = 0xFF140D1A,
            posterAccentColor = 0xFFFF754C,
            directVariants = listOf(
                StreamVariant(
                    id = "sintel_1080",
                    sourceName = "Демонстрационный поток",
                    audioTrack = "Оригинал (ENG)",
                    quality = "MP4",
                    streamUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
                )

            )
        ),
        MediaItem(
            id = "demo_tears_of_steel",
            title = "Стальные слёзы",
            originalTitle = "Tears of Steel",
            year = 2012,
            type = MediaType.MOVIE,
            rating = "7.1",
            genres = listOf("Фантастика", "Киберпанк", "Короткометражка"),
            description = "В постапокалиптическом Амстердаме группа учёных и бойцов пытается воссоздать ключевой момент прошлого, чтобы предотвратить восстание боевых роботов и спасти человечество.",
            licenseInfo = "Blender Foundation / Blender Institute • CC BY 3.0 • https://creativecommons.org/licenses/by/3.0/",
            posterColorStart = 0xFF0E2A38,
            posterColorEnd = 0xFF071117,
            posterAccentColor = 0xFF28C7FA,
            directVariants = listOf(
                StreamVariant(
                    id = "tos_1080",
                    sourceName = "Демонстрационный поток",
                    audioTrack = "Оригинал (ENG)",
                    quality = "MOV",
                    streamUrl = "https://download.blender.org/demo/movies/ToS/tears_of_steel_720p.mov"
                )
            )
        ),
        MediaItem(
            id = "demo_open_chronicles",
            title = "Хроники открытого кода",
            originalTitle = "Open Studio Chronicles",
            year = 2013,
            type = MediaType.SERIES,
            rating = "8.2",
            genres = listOf("Анимация", "Комедия", "Антология"),
            description = "Демонстрационный многосерийный проект, объединяющий открытые анимационные эпизоды студии Blender. Позволяет протестировать выбор сезона, серии, качества и озвучки в карточке сериала.",
            licenseInfo = "Blender Foundation • трейлеры Big Buck Bunny и Sintel • CC BY 3.0 • https://creativecommons.org/licenses/by/3.0/",
            posterColorStart = 0xFF1B3A2B,
            posterColorEnd = 0xFF0B1711,
            posterAccentColor = 0xFF4ADE80,
            seasons = listOf(
                SeasonItem(
                    number = 1,
                    title = "Сезон 1: Классика",
                    episodes = listOf(
                        EpisodeItem(
                            number = 1,
                            title = "Big Buck Bunny — трейлер",
                            durationMinutes = 1,
                            variants = listOf(
                                StreamVariant(
                                    id = "bbb_1080",
                                    sourceName = "Демонстрационный поток",
                                    audioTrack = "Без слов (Оригинал)",
                                    quality = "MP4",
                                    streamUrl = "https://media.w3.org/2010/05/bunny/trailer.mp4"
                                )

                            )
                        ),
                        EpisodeItem(
                            number = 2,
                            title = "Sintel — трейлер",
                            durationMinutes = 1,
                            variants = listOf(
                                StreamVariant(
                                    id = "ed_1080",
                                    sourceName = "Демонстрационный поток",
                                    audioTrack = "Оригинал (ENG)",
                                    quality = "MP4",
                                    streamUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
                                )
                            )
                        )
                    )
                )

            )
        ),

        MediaItem(
            id = "demo_big_buck_bunny",
            title = "Большой кролик Бак — трейлер",
            originalTitle = "Big Buck Bunny",
            year = 2008,
            type = MediaType.MOVIE,
            rating = "7.8",
            genres = listOf("Комедия", "Семейный", "Анимация"),
            description = "Добродушный огромный кролик наслаждается весенним утром в лесу, пока троица задир-грызунов не начинает испытывать его терпение.",
            licenseInfo = "Blender Foundation • CC BY 3.0 • https://creativecommons.org/licenses/by/3.0/",
            posterColorStart = 0xFF283044,
            posterColorEnd = 0xFF111520,
            posterAccentColor = 0xFFA78BFA,
            directVariants = listOf(
                StreamVariant(
                    id = "bbb_movie_1080",
                    sourceName = "Демонстрационный поток",
                    audioTrack = "Оригинал (Без слов)",
                    quality = "MP4",
                    streamUrl = "https://media.w3.org/2010/05/bunny/trailer.mp4"
                )
            )
        )

    )

    override suspend fun getRecentFeed(): List<MediaItem> = demoCatalog

    override suspend fun search(query: String): List<MediaItem> {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return demoCatalog
        return demoCatalog.filter { item ->
            item.title.lowercase().contains(clean) ||
                item.originalTitle.lowercase().contains(clean) ||
                item.genres.any { it.lowercase().contains(clean) }
        }
    }

    override suspend fun getDetails(mediaId: String): MediaItem? {
        return demoCatalog.find { it.id == mediaId }
    }
}
