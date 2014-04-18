package com.arcade.DroidRunner;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.e3roid.E3Activity;
import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Shape;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.sprite.AnimatedSprite;
import com.e3roid.drawable.sprite.TextSprite;
import com.e3roid.drawable.texture.TiledTexture;

public class DroidWarrior extends E3Activity{
	private static final int WIDTH=480;
	private static final int HEIGHT=320;
	TiledTexture back;
	Sprite backSprite;
	TiledTexture hero;
	AnimatedSprite heroSprite;
	TextSprite StartGame;
	TextSprite gameName;
	TextSprite Settings;
	TextSprite Quit;
	@Override
	public E3Engine onLoadEngine() {
		// TODO Auto-generated method stub
		E3Engine engine=new E3Engine(this,WIDTH,HEIGHT);
		engine.requestFullScreen();
		engine.requestLandscape();
		return engine;
	}

	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub
		back=new TiledTexture("droidwarriorback.png",500,320,this);
		hero=new TiledTexture("New.png",95,85,0,2,0,0,this);
	}

	@Override
	public E3Scene onLoadScene() {
		// TODO Auto-generated method stub
		E3Scene scene=new E3Scene();
		gameName=new TextSprite("Droid Runner",36,this);
		StartGame=new TextSprite("Start Game",24,Color.WHITE,Color.TRANSPARENT,Typeface.SANS_SERIF,this)
		{
			@Override
			public boolean onTouchEvent(E3Scene scene, Shape shape,
				      MotionEvent motionEvent, int localX, int localY)
			{
				if(motionEvent.getAction()==MotionEvent.ACTION_DOWN)
				{
					Intent i=new Intent(getBaseContext(),ArcadeDemoActivity.class);
					startActivity(i);
					return true;
				}
				return false;
			}
		};
		scene.addEventListener(StartGame);
		Settings=new TextSprite("Settings",24,Color.WHITE,Color.TRANSPARENT,Typeface.SANS_SERIF,this);
		backSprite=new Sprite(back);
		heroSprite=new AnimatedSprite(hero,10,getHeight()-100);
		gameName.setPosition(getWidth()/2,getHeight()/2);
		StartGame.setPosition(getWidth()/2,getHeight()/2+50);
		Settings.setPosition(getWidth()/2,getHeight()/2+100);
		scene.getTopLayer().add(backSprite);
		scene.getTopLayer().add(heroSprite);
		scene.getTopLayer().add(StartGame);
		scene.getTopLayer().add(Settings);
		scene.getTopLayer().add(gameName);
		return scene;
	}
}
