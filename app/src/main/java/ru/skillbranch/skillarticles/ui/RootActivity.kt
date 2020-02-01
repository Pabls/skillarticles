package ru.skillbranch.skillarticles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity() {

    private lateinit var viewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupToolbar()
        setupBottombar()
        setupSubmenu()
        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this) { newState: ArticleState -> renderUi(newState) }
        viewModel.observeNotifications(this) { renderNotifications(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val content = viewModel.currentState
        menuInflater.inflate(R.menu.search_menu, menu)
        val actionSearch = menu.findItem(R.id.action_search)
        val searchView = actionSearch.actionView as SearchView
        searchView.inputType = InputType.TYPE_CLASS_TEXT
        searchView.queryHint = getString(R.string.menu_search_hint)

        if (!content.searchQuery.isNullOrEmpty() || content.isSearch) {
            actionSearch.expandActionView()
            if (!content.searchQuery.isNullOrEmpty()) {
                searchView.setQuery(content.searchQuery, false)
            }
            if (content.isSearch) {
                searchView.requestFocus()
            } else {
                searchView.clearFocus()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })

        searchView.setOnQueryTextFocusChangeListener { _, isSearch ->
            viewModel.handleSearchMode(isSearch)
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun renderNotifications(notify: Notify) {
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)
            .setActionTextColor(getColor(R.color.color_accent_dark))

        when (notify) {
            is Notify.TextMessage -> {
            }
            is Notify.ActionMessage -> {
                snackbar.setAction(notify.actionLabel) { notify.actionHandler?.invoke() }
            }
            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(R.color.white))
                    setActionTextColor(getColor(R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()
    }


    private fun renderUi(data: ArticleState) {
        btn_settings.isChecked = data.isShowMenu
        if (data.isShowMenu) submenu.open() else submenu.close()

        btn_like.isChecked = data.isLike
        btn_bookmark.isChecked = data.isBookmark

        switch_mode.isChecked = data.isDarkMode
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (data.isBigText) {
            tv_text_content.textSize = 18f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        } else {
            tv_text_content.textSize = 14f
            btn_text_up.isChecked = false
            btn_text_down.isChecked = true
        }

        tv_text_content.text =
            if (data.isLoadingContent) resources.getString(R.string.loading)
            else data.content.first() as String

        toolbar.title = data.title ?: resources.getString(R.string.loading)
        toolbar.subtitle = data.category ?: resources.getString(R.string.loading)

        if (data.categoryIcon != null)
            toolbar.logo = getDrawable(data.categoryIcon as Int)
    }

    private fun setupSubmenu() {
        btn_text_up.setOnClickListener { viewModel.handleUpText() }
        btn_text_down.setOnClickListener { viewModel.handleDownText() }
        switch_mode.setOnClickListener { viewModel.handleNightMode() }
    }

    private fun setupBottombar() {
        btn_bookmark.setOnClickListener { viewModel.handleBookmark() }
        btn_like.setOnClickListener { viewModel.handleLike() }
        btn_settings.setOnClickListener { viewModel.handleToggleMenu() }
        btn_share.setOnClickListener { viewModel.handleShare() }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val logo = if (toolbar.childCount > 2) toolbar.getChildAt(2) as? ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP

        val lp = logo?.layoutParams as? Toolbar.LayoutParams

        lp?.let {
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }
    }
}
