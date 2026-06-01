package com.example.paktrainfoodapp.utils;

import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;

public class RefreshHelper {

    public static void setupRefresh(
            Toolbar toolbar,
            Refreshable refreshable,
            Fragment fragment
    ) {

        toolbar.inflateMenu(R.menu.menu_refresh);

        MenuItem refreshItem =
                toolbar.getMenu().findItem(R.id.action_refresh);

        ImageView refreshIcon =
                new ImageView(fragment.requireContext());

        refreshIcon.setImageDrawable(

                ContextCompat.getDrawable(
                        fragment.requireContext(),
                        android.R.drawable.ic_popup_sync
                )
        );

        Toolbar.LayoutParams params =
                new Toolbar.LayoutParams(72, 72);

        refreshIcon.setLayoutParams(params);

        refreshItem.setActionView(refreshIcon);

        toolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.action_refresh) {

                // ROTATE ICON
                Animation rotate =
                        AnimationUtils.loadAnimation(
                                fragment.requireContext(),
                                R.anim.rotate_refresh
                        );

                refreshIcon.startAnimation(rotate);

                // REAL REFRESH
                refreshable.refreshData();

                return true;
            }

            return false;
        });
    }
}