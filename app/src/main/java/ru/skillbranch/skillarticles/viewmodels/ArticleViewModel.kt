package ru.skillbranch.skillarticles.viewmodels

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify

class ArticleViewModel(private val articleId: String) : BaseViewModel<ArticleState>(ArticleState()),
        IArticleViewModel {

    private val repository = ArticleRepository

    init {
        subscribeOnDataSource(
                source = getArticleData(),
                onChanged = { article, state ->
                    article ?: return@subscribeOnDataSource null
                    state.copy(
                            shareLink = article.shareLink,
                            title = article.title,
                            category = article.category,
                            categoryIcon = article.categoryIcon,
                            date = article.date.format(),
                            author = article.author
                    )
                })

        subscribeOnDataSource(
                source = getArticleContent(),
                onChanged = { content, state ->
                    content ?: return@subscribeOnDataSource null
                    state.copy(
                            isLoadingContent = false,
                            content = content
                    )
                }
        )

        subscribeOnDataSource(
                source = getArticlePersonalInfo(),
                onChanged = { info, state ->
                    info ?: return@subscribeOnDataSource null
                    state.copy(
                            isBookmark = info.isBookmark,
                            isLike = info.isLike
                    )
                }
        )

        subscribeOnDataSource(
                source = repository.getAppSettings(),
                onChanged = { settings, state ->
                    state.copy(
                            isDarkMode = settings.isDarkMode,
                            isBigText = settings.isBigText
                    )
                }
        )
    }

    override fun getArticleContent(): LiveData<List<Any>?> =
            repository.loadArticleContent(articleId)


    override fun getArticleData(): LiveData<ArticleData?> =
            repository.getArticle(articleId)


    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> =
            repository.loadArticlePersonalInfo(articleId)


    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(
                settings.copy(isDarkMode = !settings.isDarkMode, isBigText = settings.isBigText)
        )
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    override fun handleBookmark() {
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(
                info.copy(
                        isLike = info.isLike,
                        isBookmark = !info.isBookmark
                )
        )

        val message =
                if (currentState.isBookmark) Notify.TextMessage("Add to bookmarks")
                else Notify.TextMessage("Remove from bookmarks")
        notify(message)
    }

    override fun handleLike() {
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(
                    info.copy(
                            isLike = !info.isLike,
                            isBookmark = info.isBookmark
                    )
            )
        }

        toggleLike()

        val message =
                if (currentState.isLike) Notify.TextMessage("Mark is liked")
                else Notify.ActionMessage(
                        msg = "Don`t like it anymore",
                        actionLabel = "No, still like it",
                        actionHandler = toggleLike
                )

        notify(message)
    }

    override fun handleShare() {
        notify(
                Notify.ErrorMessage(
                        msg = "Share is not implemented",
                        errLabel = "OK",
                        errHandler = null
                )
        )
    }

    override fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    override fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0) }
    }

    override fun handleSearch(query: String?) {
        query ?: return
        val result = (currentState.content.firstOrNull() as? String)
                ?.indexesOf(query)
                ?.map { it to it + query.length }
        updateState { it.copy(searchQuery = query, searchResults = result ?: emptyList()) }
    }

    fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }

    fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }
}

data class ArticleState(
        val isAuth: Boolean = false, // пользователь авторизован
        val isLoadingContent: Boolean = true, // контент загружается
        val isLoadingReviews: Boolean = true, // отзывы загружаются
        val isLike: Boolean = false, // отмечено как Like
        val isBookmark: Boolean = false, // в закладках
        val isShowMenu: Boolean = false, // отображается меню
        val isBigText: Boolean = false, // шрифт увеличен
        val isDarkMode: Boolean = false, // темный режим
        val isSearch: Boolean = false, // режим поиска
        val searchQuery: String? = null, // поисковый запрос
        val searchResults: List<Pair<Int, Int>> = emptyList(), // результаты поиска (стартовая и конечная позиции)
        val searchPosition: Int = 0, // текущая позиция найденного результата
        val shareLink: String? = null, // ссылка Share
        val title: String? = null, // заголовок статьи
        val category: String? = null, // категория
        val categoryIcon: Any? = null, // иконка категории
        val date: String? = null, // дата публикации
        val author: Any? = null, // автор статьи
        val poster: String? = null, // обложка статьи
        val content: List<Any> = emptyList(), // контент
        val reviews: List<Any> = emptyList() // комментарии
) : IViewModelState {
    override fun save(outState: Bundle) {
        outState.putAll(
                bundleOf(
                        "isSearch" to isSearch,
                        "searchQuery" to searchQuery,
                        "searchResults" to searchResults,
                        "searchPosition" to searchPosition
                )
        )
    }

    override fun restore(savedState: Bundle): ArticleState {
        return copy(
                isSearch = savedState["isSearch"] as Boolean,
                searchQuery = savedState["searchQuery"] as? String,
                searchResults = savedState["searchResults"] as List<Pair<Int, Int>>,
                searchPosition = savedState["searchPosition"] as Int
        )
    }
}