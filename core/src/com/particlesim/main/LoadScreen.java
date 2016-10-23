package com.particlesim.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.particlesim.graphics.Assets;

public class LoadScreen implements Screen {

	ShapeRenderer sr;
	OrthographicCamera camera;

	Game game;

	public LoadScreen(Game game) {
		this.game = game;
		Assets.initialize();
		sr = new ShapeRenderer();
		camera = new OrthographicCamera();
	}

	@Override
	public void render(float delta) {
		if (Assets.manager.update()) {
			Assets.load();
			game.setScreen(new MainMenu(game));
			return;
		}
		
		float percent = Assets.manager.getProgress();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		sr.setColor(Color.WHITE);
		sr.begin(ShapeType.Line);
		sr.rect((Gdx.graphics.getWidth() - 105) * 0.5F,
				(Gdx.graphics.getHeight() - 25) * 0.5F, 105, 25);
		sr.end();
		sr.begin(ShapeType.Filled);
		sr.rect((Gdx.graphics.getWidth() - 100) * 0.5F,
				(Gdx.graphics.getHeight() - 20) * 0.5F, 100 * percent, 20);
		sr.end();
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;

		camera.position.x = camera.viewportWidth / 2;
		camera.position.y = camera.viewportHeight / 2;
		camera.update();

		sr.setProjectionMatrix(camera.combined);
	}

	@Override
	public void show() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		sr.dispose();
	}

}
