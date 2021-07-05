package com.jesen.cod.jetpackvideo.ui.mine

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.jesen.cod.jetpackvideo.R
import com.jesen.cod.libcommon.utils.StatusBarUtil

class UserFavoriteHistoryActivity : AppCompatActivity() {

    companion object {
        const val KEY_BEHAVIOR_TYPE = "behavior"
        const val BEHAVIOR_FAVORITE = 0
        const val BEHAVIOR_HISTORY = 1

        @JvmStatic
        fun startActivity(context: Context, behavior: Int) {
            val intent = Intent(context, UserFavoriteHistoryActivity::class.java)
            intent.putExtra(KEY_BEHAVIOR_TYPE, behavior)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtil.fitSystemBar(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_favorite_history)

        val behaviorType = intent.getIntExtra(KEY_BEHAVIOR_TYPE, BEHAVIOR_FAVORITE);
        val fragment = FavoriteHistoryFragment.newInstance(behaviorType)
        supportFragmentManager.beginTransaction().add(R.id.contanier, fragment, "userBehavior")
            .commit()

        val titleTv = findViewById<TextView>(R.id.title)
        when (behaviorType) {
            BEHAVIOR_FAVORITE -> titleTv.text = "收藏列表"
            BEHAVIOR_HISTORY -> titleTv.text = "浏览历史"
            else -> {
            }
        }

        findViewById<ImageView>(R.id.back_btn).setOnClickListener {
            onBackPressed()
        }
    }
}