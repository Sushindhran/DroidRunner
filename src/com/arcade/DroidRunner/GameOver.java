package com.arcade.DroidRunner;

import android.content.Intent;
import android.view.MotionEvent;

import com.e3roid.E3Activity;
import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Shape;
import com.e3roid.drawable.sprite.AnimatedSprite;
import com.e3roid.drawable.sprite.TextSprite;
import com.e3roid.drawable.texture.TiledTexture;

public class GameOver extends E3Activity {
	private static final int WIDTH=480;
	private static final int HEIGHT=320;
	TiledTexture spriteTexture;
	AnimatedSprite sprite;
	TextSprite gameOver;
	TextSprite redeem;
	
	@Override
	public E3Engine onLoadEngine() {		
		E3Engine engine=new E3Engine(this,WIDTH,HEIGHT);
		engine.requestFullScreen();
		engine.requestLandscape();		
		return engine;
	}

	@Override
	public void onLoadResources() {
		spriteTexture=new TiledTexture("New.png",95,85,0,2,0,0,this);		
	}

	@Override
	public E3Scene onLoadScene() {
		E3Scene scene = new E3Scene();
		sprite = new AnimatedSprite(spriteTexture,10,getHeight()-100);
		gameOver = new TextSprite("You killed me!",32,this);
		gameOver.setPosition(getWidth()/2-100,getHeight()/2);
		redeem = new TextSprite("Redeem yourself",32,this){
			@Override
			public boolean onTouchEvent(E3Scene scene, Shape shape,
				      MotionEvent motionEvent, int localX, int localY)
			{
				if(motionEvent.getAction()==MotionEvent.ACTION_DOWN)
				{
					Intent i=new Intent(getBaseContext(),DroidWarrior.class);
					startActivity(i);
					return true;
				}
				return false;
			}
		};
		redeem.setPosition(getWidth()/2-100, getHeight()/2 + 50);
		scene.addEventListener(redeem);
		scene.getTopLayer().add(gameOver);
		scene.getTopLayer().add(redeem);
		scene.getTopLayer().add(sprite);
		return scene;
	}
}
