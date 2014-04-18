package com.arcade.DroidRunner;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.AsyncTask;
import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.e3roid.E3Activity;
import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Shape;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.sprite.AnimatedSprite;
import com.e3roid.drawable.sprite.TextSprite;
import com.e3roid.drawable.texture.AssetTexture;
import com.e3roid.drawable.texture.TiledTexture;
import com.e3roid.drawable.tmx.TMXException;
import com.e3roid.drawable.tmx.TMXLayer;
import com.e3roid.drawable.tmx.TMXProperty;
import com.e3roid.drawable.tmx.TMXTile;
import com.e3roid.drawable.tmx.TMXTiledMap;
import com.e3roid.drawable.tmx.TMXTiledMapLoader;
import com.e3roid.event.SceneUpdateListener;
import com.e3roid.physics.PhysicsShape;
import com.e3roid.physics.PhysicsWorld;
import com.e3roid.util.Debug;
import com.e3roid.util.MathUtil;

public class ArcadeDemoActivity extends E3Activity implements SceneUpdateListener{

	private static final int WIDTH=480;
	private static final int HEIGHT=320;	
	private int moveCount;
	private int jumpCount;
	private int crouchCount;
	int i=0;
	private int xstep;
	private int ystep;
	private int tempMapX;
	TMXLayer collisionLayer;
	private TMXTiledMap map;
	private ArrayList<TMXLayer> mapLayers;	
	private int spritePosX;
	private int spritePosY;
	private int mapStartX = 0;
	private TextSprite score;
	TiledTexture texture;
	AssetTexture block;	
	TiledTexture bTexture;
	TiledTexture rTexture;
	TiledTexture gTexture;
	TiledTexture oTexture;
	Sprite punchButtonSprite;
	Sprite jumpButtonSprite;
	Sprite moveButtonSprite;
	Sprite crouchButtonSprite;	
	ArrayList<AnimatedSprite.Frame> frames=new ArrayList<AnimatedSprite.Frame>();
	ArrayList<AnimatedSprite.Frame> lframes=new ArrayList<AnimatedSprite.Frame>();
	ArrayList<AnimatedSprite.Frame> rframes=new ArrayList<AnimatedSprite.Frame>();
	ArrayList<AnimatedSprite.Frame> dframes=new ArrayList<AnimatedSprite.Frame>();
	ArrayList<AnimatedSprite.Frame> mframes=new ArrayList<AnimatedSprite.Frame>();
	ArrayList<AnimatedSprite.Frame> faintFrame=new ArrayList<AnimatedSprite.Frame>();
	ArrayList<AnimatedSprite.Frame> fightFrame=new ArrayList<AnimatedSprite.Frame>();
	final E3Scene scene=new E3Scene();
	AnimatedSprite sprite1;		
	private boolean run = false;
	private boolean jump = false;
	private boolean crouch = false;
	private boolean resetCrouch = false;
	private boolean resetJump=false;
	private boolean gameStart = true;
	AnimateSprite animateSprite;
	private PhysicsWorld world;
	PhysicsShape prevBlock;
	TiledTexture coin;
	Sprite coinSprite;
	Sprite prevBlockSprite;
	int counter;
	private SoundPool soundPool;
	private int soundId;
	private int soundCoin;
	private boolean soundLoaded;

	@Override
	public E3Engine onLoadEngine() {
		// TODO Auto-generated method stub
		System.out.println("Inside On load engine");
		E3Engine engine=new E3Engine(this,WIDTH,HEIGHT);
		engine.requestFullScreen();
		engine.requestLandscape();
		return engine;
	}

	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub		
		System.out.println("Inside On load resources");
		texture=new TiledTexture("New.png",95,85,0,2,0,0,this);
		System.out.println("Texture "+texture.getTileHeight());
		bTexture=new TiledTexture("Blue.png",40,40,this);
		rTexture= new TiledTexture("Red.png",40,40,this);		
		gTexture= new TiledTexture("Green.png",40,40,this);
		oTexture = new TiledTexture("Orange.png",40,40,this);
		world   = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		block = new AssetTexture("block.png", this);
		block.setReusable(true);
		coin = new TiledTexture("Coin.png",40,40,this);
		coin.setReusable(true);
		
		
		//Loading the Sound files
				soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
				soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

					public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
						// TODO Auto-generated method stub
						soundLoaded = true;
					}
				});
				soundId = soundPool.load(getBaseContext(), R.raw.explosion, 1);
				soundCoin = soundPool.load(getBaseContext(),R.raw.cion,1);
	}

	@Override
	public E3Scene onLoadScene() {
		// TODO Auto-generated method stub
		System.out.println("Inside On load scene");
		scene.addEventListener(this);
		scene.registerUpdateListener(10,this);
		scene.registerUpdateListener(10, world);
		sprite1=new AnimatedSprite(texture,80,getHeight()-texture.getTileHeight()-64){			
			@Override
			public Rect getCollisionRect() {
				// character's collision rectangle is just around his body.
				Rect rect = this.getRect();
				if(crouch){
					rect.left   = rect.left   + this.getWidth() / 3;
					rect.right  = rect.right  - this.getWidth() / 3;
					rect.top    = rect.top    + this.getHeight() /3;
					rect.bottom = rect.bottom - this.getHeight()/5;
				}
				else{
					rect.left   = rect.left   + this.getWidth() / 3;
					rect.right  = rect.right  - this.getWidth() / 3;
					rect.top    = rect.top    + this.getHeight() / 30;
					rect.bottom = rect.bottom - this.getHeight()/5;
				}
				return rect;
			}
		};

		int size = 48;
		Shape ground = new Shape(0, getHeight() - size, getWidth(), size);
		final FixtureDef wallFixtureDef = createFixtureDef(0.0f, 0.0f, 0.5f);
		createBoxBody(this.world, ground, BodyType.DynamicBody, wallFixtureDef);
		scene.getTopLayer().add(ground);

		final FixtureDef spriteFixtureDef = createFixtureDef(1.0f, 0.0f, 0.5f);
		Body body = createBoxBody(this.world, sprite1, BodyType.StaticBody, spriteFixtureDef);
		animateSprite = new AnimateSprite(this, sprite1);
		world.addShape(new PhysicsShape(sprite1, body));
		coinSprite = new Sprite(coin, this.getWidth()/2, getHeight()/2-60);

		//For punch

		punchButtonSprite=new Sprite(rTexture, getWidth()-rTexture.getWidth()-10,getHeight()-2*rTexture.getHeight()-10)		
		{
			@Override
			public boolean onTouchEvent(E3Scene scene,Shape shape,MotionEvent event,int localX,int localY)
			{
				animateSprite.punch(event);
				return true;
			}
		};

		
			
		//For Move
		moveButtonSprite = new Sprite(gTexture, 5,getHeight()-gTexture.getHeight()){
			@Override
			public boolean onTouchEvent(E3Scene scene,Shape shape,MotionEvent event,int localX,int localY)
			{

				moveCount++;
				if(moveCount%2==1){
					if(run==false && event.getAction()==MotionEvent.ACTION_DOWN){
						System.out.println("Here");
						animateSprite.runAnimation();
						run=true;
					}
					else if(run==true && event.getAction()==MotionEvent.ACTION_DOWN){
						System.out.println("Here else");
						animateSprite.stopAnimation();
						run=false;
					}
				}
				return true;
			}
		};


		//For Jump
		jumpButtonSprite = new Sprite(bTexture, getWidth()-bTexture.getWidth()-10,getHeight()-bTexture.getHeight()){
			@Override
			public boolean onTouchEvent(E3Scene scene,Shape shape,MotionEvent event,int localX,int localY)
			{

				jumpCount++;
				if(jumpCount%2==1 &&event.getAction()==MotionEvent.ACTION_DOWN){
					jump=true;								
				}				
				return true;
			}
		};

		//For crouch		
		crouchButtonSprite = new Sprite(oTexture, 5,getHeight()-bTexture.getHeight()-oTexture.getHeight()-10){
			@Override
			public boolean onTouchEvent(E3Scene scene,Shape shape,MotionEvent event,int localX,int localY)
			{				
				if(run){
					crouchCount++;
					if(crouchCount%2==1 && event.getAction()==MotionEvent.ACTION_DOWN){
						crouch=!crouch;
						if(!crouch){
							resetCrouch = true;
						}
					}					
				}
				else{
					animateSprite.crouch(event);					
				}
				return true;
			}
		};


		/*final TextSprite loadingText = new TextSprite("Loading map...", 24, this);
		loadingText.move((getWidth()  - loadingText.getWidth())/2,(getHeight() - loadingText.getHeight())/2);
		loadingText.addModifier(new SpanModifier(500L, new AlphaModifier(0, 0, 1)));
		System.out.println("loading text");
		scene.getTopLayer().add(loadingText);*/

		// limit refresh rate while loading for saving cpu power
		engine.setRefreshMode(E3Engine.REFRESH_LIMITED);
		engine.setPreferredFPS(10);

		new AsyncTask<Void,Integer,TMXTiledMap>() {
			@Override
			protected TMXTiledMap doInBackground(Void... params) {
				// get the map from TMX map file.
				try {
					TMXTiledMapLoader mapLoader = new TMXTiledMapLoader();
					TMXTiledMap map = mapLoader.loadFromAsset("Map1.tmx", ArcadeDemoActivity.this);
					return map;
				} catch (TMXException e) {
					Debug.e(e.getMessage());
				}
				return null;
			}

			@Override
			protected void onPostExecute(TMXTiledMap tmxTiledMap) {
				System.out.println("inside onPostExecute");
				map = tmxTiledMap;

				if (tmxTiledMap != null && (mapLayers = map.getLayers()) != null) {
					System.out.println("Map "+map.getColumns()+" "+map.getRows());
					for (TMXLayer layer : mapLayers) {
						System.out.println("Inside loop" + ++i);
						// Determine scene size of the layer.
						layer.loop(true);
						// This enables layer to skip drawing the tile which is out of the screen.
						layer.setSceneSize(getWidth(), getHeight());

						if("Ground".equals(layer.getName()))
						{
							layer.addChild(sprite1);
						}

						if("Collision".equals(layer.getName()))
						{
							collisionLayer=layer;
							continue;
						}

						scene.getTopLayer().add(layer);						
						scene.getTopLayer().add(sprite1);						
						//scene.getTopLayer().remove(loadingText);
						scene.getTopLayer().add(punchButtonSprite);
						scene.addEventListener(punchButtonSprite);
						scene.getTopLayer().add(jumpButtonSprite);
						scene.addEventListener(jumpButtonSprite);
						scene.getTopLayer().add(moveButtonSprite);
						scene.addEventListener(moveButtonSprite);
						scene.getTopLayer().add(crouchButtonSprite);
						scene.getTopLayer().add(coinSprite);
						scene.addEventListener(crouchButtonSprite);
						engine.setRefreshMode(E3Engine.REFRESH_DEFAULT);
					}
				}
				else
				{
					/*loadingText.setText("Failed to load!");
					loadingText.setAlpha(1);
					loadingText.clearModifier();*/
				}
			}
		}.execute();
		return scene;
	}




	private boolean isInTheScene(Sprite sprite, int xstep, int ystep) {		
		spritePosX = sprite.getRealX() + xstep;
		spritePosY = sprite.getRealY() - 32 - texture.getTileHeight();
		System.out.println("sprite "+spritePosY+" "+(getHeight()-texture.getTileHeight()));
		if(spritePosX > 0 && (spritePosY < getHeight()- texture.getTileHeight()))
		{			
			return true;
		} 
		else
		{
			return false;
		}
	}

	private boolean collidesWithTile(AnimatedSprite sprite, int xstep, int ystep) {
		if (collisionLayer == null){ 
			return false;
		}		
		return collisionLayer.getTileFromRect(sprite.getCollisionRect(), xstep/2, ystep/2).size() != 0;
	}


	private boolean collidesWithCoin(AnimatedSprite sprite) {
		System.out.println("Inside Coin check");
		System.out.println("Coin "+sprite.getCollisionRect()+" "+coinSprite.getCollisionRect());
		if(sprite.getCollisionRect().intersect(coinSprite.getCollisionRect()))
			return true;
		else
			return false;		
	}

	private boolean collidesWithBlock(AnimatedSprite sprite) {
		if(prevBlockSprite!=null){
			System.out.println("Inside Block check");
			System.out.println("Block "+sprite.getCollisionRect()+" "+prevBlockSprite.getCollisionRect());
			if(sprite.getCollisionRect().intersect(prevBlockSprite.getCollisionRect()))
				return true;		
		}
		return false;
	}


	public void onUpdateScene(E3Scene arg0, long arg1) {		
		if(gameStart){
			xstep=1;
			ystep=0;

			counter++;
			if(counter==399){
				counter=0;		
				if(prevBlock!=null && prevBlockSprite!=null){
					world.removeShape(prevBlock);
					scene.getTopLayer().remove(prevBlockSprite);					
				}
				postUpdate(new AddShapeImpl(scene, this.getWidth()/2, 10));
				scene.getTopLayer().remove(coinSprite);
				coinSprite = new Sprite(coin, this.getWidth()-50, getHeight()/2-40);
				scene.getTopLayer().add(coinSprite);

			}

			/*if(!isInTheScene(coinSprite, 0,	0)){
			scene.getTopLayer().remove(coinSprite);
		}*/

			if(collidesWithCoin(sprite1)){
				System.out.println("Coin collected");
				//Playing Sound on hitting a Balloon
				AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
				float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				float volume = actualVolume / maxVolume;
				// Is the sound loaded already?
				if (soundLoaded) {
					soundPool.play(soundCoin, volume, volume, 1, 0, 1f);
				}

				coinSprite.hide();
			}


			System.out.println("Inside on update scene");
			if(mapLayers!=null)
			{
				for(TMXLayer layer:mapLayers)
				{

					TMXTile underTile = layer.getTileFromPosition(
							sprite1.getRealX() + (sprite1.getWidth()),
							sprite1.getRealY() + sprite1.getHeight()+5);		

					if(TMXTile.isEmpty(underTile))
					{					
						if(!jump){
							//System.out.println("JUMPPPPPPPPP");
							//sprite1.hide();
						}
					}
					else{

						ArrayList<TMXProperty> props = map.getTileProperties(underTile.getGID());
						if (props != null) {
							for(TMXProperty prop : props) {							
								if ("Collision".equals(prop.getName()) 
										&& "true".equals(prop.getValue())) {
									// land the dog on the collidable tile.
									sprite1.move(xstep,ystep);
								}
							}
						}
					}
				}
			}

			if(run){			
				// move the ground
				if ((!collidesWithTile(sprite1, xstep, ystep)) && (spritePosX>getWidth()/2-sprite1.getWidth())) {
					System.out.println("Ground is moving");

					//Move the coin
					coinSprite.move(coinSprite.getRealX()-2, coinSprite.getRealY());

					//Move the block
					if(prevBlockSprite!=null){
						PhysicsShape pShape = world.findShape(prevBlockSprite);
						if(pShape!=null)
							pShape.getBody().setLinearVelocity(new Vector2(-2, 1));

					}

					for (TMXLayer layer : mapLayers) {
						if(jump)
							mapStartX += 4;
						else
							mapStartX += 2;
						layer.setPosition(mapStartX, layer.getY());
						mapStartX = layer.getX();
					}
				}

				if(collidesWithBlock(sprite1)){
					System.out.println("Toast");
					//Playing Sound on hitting a Balloon
					AudioManager audioManager = (AudioManager)getBaseContext().getSystemService(Context.AUDIO_SERVICE);
					float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
					float volume = actualVolume / maxVolume;
					// Is the sound loaded already?
					if (soundLoaded) {
						soundPool.play(soundId, volume, volume, 1, 0, 1f);
					}

					scene.getTopLayer().remove(sprite1);
					gameStart = false;								
					
				}

				// move the sprite
				if (!collidesWithTile(sprite1, xstep, ystep) && isInTheScene(sprite1, xstep, ystep) &&!(spritePosX>getWidth()/2-sprite1.getWidth())) {						
					System.out.println("Sprite is moving");
					//sprite1.move(sprite1.getRealX()+xstep, sprite1.getRealY());
					PhysicsShape pShape = world.findShape(sprite1);
					pShape.getBody().setLinearVelocity(new Vector2(1, 0));
					System.out.println(sprite1.getRealX()+" "+sprite1.getRealY()+" "+xstep);
				}
				else{
					if(jump){
						//sprite1.move(sprite1.getRealX(), sprite1.getRealY()-70);
						PhysicsShape pShape = world.findShape(sprite1);
						pShape.getBody().setLinearVelocity(new Vector2(0, -SensorManager.GRAVITY_THE_ISLAND));

						System.out.println("Jump true");
						sprite1.setTile(1,2);
						tempMapX = mapStartX;
						jump = false;
						resetJump=true;
					}
					else if(crouch){
						animateSprite.stopAnimation();
						sprite1.setTile(0,1);		
					}
					else{
						if(resetJump && mapStartX>tempMapX+50){
							//sprite1.move(sprite1.getRealX(), sprite1.getRealY()+70);
							System.out.println("Jump reset");
							resetJump=false;
						}
						else if(resetCrouch){						
							resetCrouch = false;
							animateSprite.runAnimation();				
						}
						else{
							sprite1.move(sprite1.getRealX(), sprite1.getRealY());						
						}
					}
				}		
			}
			else{		
				if(jump == true){				
					sprite1.move(sprite1.getRealX(),sprite1.getRealY()-70);
					jump=false;
					resetJump=true;
				}
				else{
					if(resetJump){
						sprite1.move(sprite1.getRealX(),sprite1.getRealY()+70);
						resetJump=false;
					}
				}
			}
		}
		else{
			
			Intent i=new Intent(getBaseContext(),GameOver.class);
			startActivity(i);
			
		}
	}	

	class AddShapeImpl implements Runnable {
		private final E3Scene scene;
		private final int x;
		private final int y;
		AddShapeImpl(E3Scene scene, int x, int y) {
			this.scene = scene;
			this.x = x;
			this.y = y;
		}
		@Override
		public void run() {
			//onUpdateScene(this.scene, 1);
			FixtureDef objectFixtureDef = createFixtureDef(1.0f, 0.0f, 0.5f);


			Sprite sprite = newSprite(x, y);

			Body body = createBoxBody(
					world, sprite, BodyType.DynamicBody, objectFixtureDef);

			prevBlock = new PhysicsShape(sprite, body);
			prevBlockSprite = sprite;

			world.addShape(prevBlock);
			scene.getTopLayer().add(prevBlockSprite);
		}
	}

	private FixtureDef createFixtureDef(float density, float restitution, float friction) {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = density;
		fixtureDef.restitution = restitution;
		fixtureDef.friction = friction;
		fixtureDef.isSensor = false;
		return fixtureDef;
	}

	private Body createBoxBody(PhysicsWorld physicsWorld, Shape shape,
			BodyType bodyType, FixtureDef fixtureDef) {
		float pixelToMeterRatio = PhysicsWorld.PIXEL_TO_METER_RATIO_DEFAULT;
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = bodyType;

		float[] sceneCenterCoordinates = shape.getGlobalCenterCoordinates();
		boxBodyDef.position.x = sceneCenterCoordinates[0] / (float)pixelToMeterRatio;
		boxBodyDef.position.y = sceneCenterCoordinates[1] / (float)pixelToMeterRatio;

		Body boxBody = physicsWorld.createBody(boxBodyDef);
		PolygonShape boxPoly = new PolygonShape();

		float halfWidth = shape.getWidthScaled() * 0.5f / pixelToMeterRatio;
		float halfHeight = shape.getHeightScaled() * 0.5f / pixelToMeterRatio;

		boxPoly.setAsBox(halfWidth, halfHeight);
		fixtureDef.shape = boxPoly;
		boxBody.createFixture(fixtureDef);
		boxPoly.dispose();

		boxBody.setTransform(boxBody.getWorldCenter(), MathUtil.degToRad(shape.getAngle()));

		return boxBody;
	}

	private Sprite newSprite(int x, int y) {
		return new Sprite(block, x, y) {
			@Override
			public Rect getCollisionRect(){
				Rect rect = this.getRect();
				
					rect.left   = rect.left   - this.getWidth()/3;
					rect.right  = rect.right  + this.getWidth()/3;
					rect.top    = rect.top    - this.getHeight()/3;
					rect.bottom = rect.bottom + this.getHeight()/3;				
				return rect;
			}
		};
	}

}