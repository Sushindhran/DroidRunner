package com.arcade.DroidRunner;

import java.util.ArrayList;

import android.content.Context;
import android.view.MotionEvent;

import com.e3roid.drawable.sprite.AnimatedSprite;
import com.e3roid.drawable.texture.TiledTexture;

public class AnimateSprite{

	private AnimatedSprite sprite;
	private TiledTexture texture;
	private ArrayList<AnimatedSprite.Frame> runFrames;
	
	private boolean punch = false;
	private boolean crouch = false;
	private Context context;

	public AnimateSprite(Context context,AnimatedSprite sprite){
		texture=new TiledTexture("New.png",95,85,0,2,0,0,context);
		//For run animation
		this.sprite = sprite;
		this.context = context;
		runFrames=new ArrayList<AnimatedSprite.Frame>();
		runFrames.add(new AnimatedSprite.Frame(0,0));
		runFrames.add(new AnimatedSprite.Frame(1,0));
		runFrames.add(new AnimatedSprite.Frame(2,0));
		runFrames.add(new AnimatedSprite.Frame(3,0));
		runFrames.add(new AnimatedSprite.Frame(2,0));
		
	}	

	public void crouch(MotionEvent event){
		if(crouch==false && event.getAction()==MotionEvent.ACTION_DOWN){					
			crouch=true;
			sprite.setTile(2,1);				
		}
		if(crouch==true && event.getAction()==MotionEvent.ACTION_UP){
			crouch=false;
			sprite.setTile(0,2);					
		}		
	}

	public void punch(MotionEvent event){
		if(punch==false && event.getAction()==MotionEvent.ACTION_DOWN){
			punch=true;
			sprite.setTile(3, 2);
		}
		if(punch==true && event.getAction()==MotionEvent.ACTION_UP){				
			sprite.stop();
			punch=false;
			sprite.setTile(0,2);
		}
	}
	
	

	public void runAnimation(){
		sprite.animate(200, runFrames);
	}

	public void stopAnimation(){
		if(sprite.isAnimated()){
			sprite.stop();
			sprite.setTile(0,2);
		}
	}
}
