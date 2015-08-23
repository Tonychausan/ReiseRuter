package com.reise.ruter.support;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.reise.ruter.R;

public class AnimationSupport {
	public static void settingDownSlider(Context ctx, View v){
		Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_down);
		if(a != null){
			a.reset();
			if(v != null){
				v.clearAnimation();
				v.startAnimation(a);
			}
		}
	}
	public static void settingUpSlider(Context ctx, View v){
		Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_down);
		if(a != null){
			a.reset();
			if(v != null){
				v.clearAnimation();
				v.startAnimation(a);
			}
		}
	}


}
