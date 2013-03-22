package com.group6.penguinsgame;

/**
 *   Copyright 2013 Aaron Horn aaron@cs.txstate.edu
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * Source:
 * 
 */

import aurelienribon.bodyeditor.BodyEditorLoader;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.me.mypenguins.model.GameWorld;
import com.me.mypenguins.screens.TiledMapHelper;
import com.me.mypenguins.view.WorldRenderer;


public class Penguins implements ApplicationListener, InputProcessor, Screen {
	
	private GameWorld w;
	private WorldRenderer renderer;
    
	private int width;
	private int height;
	/**
	 * The time the last frame was rendered, used for throttling framerate
	 */
	private long lastRender;
	private TiledMapHelper tiledMapHelper;

	/**
	 * This is the main box2d "container" object. All bodies will be loaded in
	 * this object and will be simulated through calls to this object.
	 */
	private World world;

	/**
	 * This box2d debug renderer comes from libgdx test code. It draws lines
	 * over all collision boundaries, so it is immensely useful for verifying
	 * that the world collisions are as you expect them to be. It is, however,
	 * slow, so only use it for testing.
	 */
	private Box2DDebugRenderer debugRenderer;

	/**
	 * Box2d works best with small values. If you use pixels directly you will
	 * get weird results -- speeds and accelerations not feeling quite right.
	 * Common practice is to use a constant to convert pixels to and from
	 * "meters".
	 */
	public static final float PIXELS_PER_METER = 60.0f;

	/**
	 * The screen's width and height. This may not match that computed by
	 * libgdx's gdx.graphics.getWidth() / getHeight() on devices that make use
	 * of on-screen menu buttons.
	 */
	private int screenWidth;
	private int screenHeight;

	public Penguins() {
		super();

		// Defer until create() when Gdx is initialized.
		screenWidth = -1;
		screenHeight = -1;
	}

	public Penguins(int width, int height) {
		super();

		screenWidth = width;
		screenHeight = height;
	}

	@Override
	public void create() {
		/**
		 * If the viewport's size is not yet known, determine it here.
		 */
		if (screenWidth == -1) {
			screenWidth = Gdx.graphics.getWidth();
			screenHeight = Gdx.graphics.getHeight();
		}

		tiledMapHelper = new TiledMapHelper();

		tiledMapHelper.setPackerDirectory("data/packer");

		tiledMapHelper.loadMap("data/world/level1/level.tmx");

		tiledMapHelper.prepareCamera(screenWidth, screenHeight);

		world = new World(new Vector2(0.0f, -10.0f), true);

		tiledMapHelper.loadCollisions("data/collisions.txt", world,
				PIXELS_PER_METER);
	    
		debugRenderer = new Box2DDebugRenderer();

		lastRender = System.nanoTime();
	}

	@Override
	public void resume() {
	}

	@Override
	public void render() {
		long now = System.nanoTime();
		/**
		 * Have box2d update the positions and velocities (and etc) of all
		 * tracked objects. The second and third argument specify the number of
		 * iterations of velocity and position tests to perform -- higher is
		 * more accurate but is also slower.
		 */
		world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		/**
		 * A nice(?), blue backdrop.
		 */
		Gdx.gl.glClearColor(0, 0.5f, 0.9f, 0);

		/**
		 * The camera is now controlled primarily by the position of the main
		 * character, and secondarily by the map boundaries.
		 */
        
/*		tiledMapHelper.getCamera().position.x = PIXELS_PER_METER
				* penguin.getPosition().x;
*/
		/**
		 * Ensure that the camera is only showing the map, nothing outside.
		 */
		if (tiledMapHelper.getCamera().position.x < Gdx.graphics.getWidth() / 2) {
			tiledMapHelper.getCamera().position.x = Gdx.graphics.getWidth() / 2;
		}
		if (tiledMapHelper.getCamera().position.x >= tiledMapHelper.getWidth()
				- Gdx.graphics.getWidth() / 2) {
			tiledMapHelper.getCamera().position.x = tiledMapHelper.getWidth()
					- Gdx.graphics.getWidth() / 2;
		}

		if (tiledMapHelper.getCamera().position.y < Gdx.graphics.getHeight() / 2) {
			tiledMapHelper.getCamera().position.y = Gdx.graphics.getHeight() / 2;
		}
		if (tiledMapHelper.getCamera().position.y >= tiledMapHelper.getHeight()
				- Gdx.graphics.getHeight() / 2) {
			tiledMapHelper.getCamera().position.y = tiledMapHelper.getHeight()
					- Gdx.graphics.getHeight() / 2;
		}

		tiledMapHelper.getCamera().update();
		tiledMapHelper.render();

		/**
		 * Draw this last, so we can see the collision boundaries on top of the
		 * sprites and map.
		 */
		debugRenderer.render(world, tiledMapHelper.getCamera().combined.scale(
				DemoMap.PIXELS_PER_METER,
				DemoMap.PIXELS_PER_METER,
				DemoMap.PIXELS_PER_METER));

		now = System.nanoTime();
		if (now - lastRender < 30000000) { // 30 ms, ~33FPS
			try {
				Thread.sleep(30 - (now - lastRender) / 1000000);
			} catch (InterruptedException e) {
			}
		}

		lastRender = now;
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void dispose() {
	}

	@Override
public boolean keyDown(int keycode) {
// TODO Auto-generated method stub
return false;
}

@Override
public boolean keyUp(int keycode) {
// TODO Auto-generated method stub
return false;
}

@Override
public boolean keyTyped(char character) {
// TODO Auto-generated method stub
return false;
}

@Override
public boolean touchDown(int x, int y, int pointer, int button) {
boolean firstFingerTouching = Gdx.input.isTouched(0);

float xpos = x / w.BOX_TO_WORLD_WIDTH;
float ypos = (height - y) / w.BOX_TO_WORLD_HEIGHT;

xpos += (renderer.getCam().position.x - (renderer.CAMERA_VIEW_WIDTH / 2));
ypos += (renderer.getCam().position.y - (renderer.CAMERA_VIEW_HEIGHT / 2));

w.checkBall(xpos, ypos);
if(!w.penguinClicked){
w.checkSlope(xpos, ypos);//checks to see if the pointer is trying to drag the slope
}
return true;
}

@Override
public boolean touchUp(int x, int y, int pointer, int button) {
// TODO Auto-generated method stub
return false;
}

@Override
    /*
The first part of this function is a little weird so I think it's
worth explaining.
The x and y parameters that are received = the pixel coordinates.
To convert to the box2d coordinates(meters), they must be divided by the
number of pixels per meter(which has been set elsewhere).
Since the converted coordinates are only relative to the current screen
they have to be added to the absolute box2d world coordinates that are
displayed at (0,0) on the screen.
*/
public boolean touchDragged(int x, int y, int pointer) {
float xpos = x / w.BOX_TO_WORLD_WIDTH;
float ypos = (height - y) / w.BOX_TO_WORLD_HEIGHT;

xpos += (renderer.getCam().position.x - (renderer.getCam().viewportWidth / 2));
ypos += (renderer.getCam().position.y - (renderer.getCam().viewportHeight / 2));

if(!w.penguinClicked){
w.checkSlope(xpos, ypos);//checks to see if the pointer is trying to drag the slope
}
return false;
}

@Override
public boolean touchMoved(int x, int y) {
// TODO Auto-generated method stub
return false;
}

@Override
public boolean scrolled(int amount) {
// TODO Auto-generated method stub
return false;
}

@Override
public void render(float delta) {
	// TODO Auto-generated method stub
	
}

@Override
public void show() {
	// TODO Auto-generated method stub
	
}

@Override
public void hide() {
	// TODO Auto-generated method stub
	
}

@Override
public boolean mouseMoved(int screenX, int screenY) {
	// TODO Auto-generated method stub
	return false;
}
}