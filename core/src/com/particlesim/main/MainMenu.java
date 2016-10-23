package com.particlesim.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.particlesim.graphics.Assets;
import com.particlesim.input.SimulationUI;

public class MainMenu implements Screen {

	Table screenTable;
	Table table;
	Stage stage;
	SpriteBatch batch;
	Viewport viewport;

	OrthographicCamera camera;

	Game game;

	public MainMenu(Game _game) {
		game = _game;

		camera = new OrthographicCamera();

		stage = new Stage();
		viewport = new ScreenViewport();
		stage.setViewport(viewport);

		Gdx.input.setInputProcessor(stage);

		batch = new SpriteBatch();

		screenTable = new Table();
		screenTable.setFillParent(true);
		stage.addActor(screenTable);

		table = new Table();
		// table.setDebug(true);
		screenTable.add(table).width(500).height(300);

		TextButton btnStart = new TextButton("Start", Assets.defaultSkin);
		btnStart.addListener(new ClickListener() {
			public void clicked(InputEvent e, float x, float y) {
				SimulationUI sim = new SimulationUI(game);
				sim.initialize(SimulationUI.STANDARD);
				game.setScreen(sim);
			}
		});

		TextButton btnLoad = new TextButton("Load", Assets.defaultSkin);
		btnLoad.addListener(new ClickListener() {
			public void clicked(InputEvent e, float x, float y) {
				SimulationUI sim = new SimulationUI(game);
				if(sim.initialize(SimulationUI.LOAD))
				game.setScreen(sim);
			}
		});

		LabelStyle lblTitleStyle = new LabelStyle();
//		FreeTypeFontGenerator lblTitleFont = new FreeTypeFontGenerator(
//				Gdx.files.internal("GUI/MYRIADPRO-REGULAR.OTF"));
//		;
//		lblTitleStyle.font = lblTitleFont.generateFont(72);
//		lblTitleFont.dispose();
//		Label lblTitle = new Label("Particle Sim 2D", lblTitleStyle);

//		table.add(lblTitle).spaceBottom(50);
		table.row();
		table.add(btnStart).expandX().fillX().height(50).padBottom(10);
		table.row();
		table.add(btnLoad).fillX().height(50);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		Assets.backgroundSprite.draw(batch);
		batch.end();

		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		if (width / (float) height > Assets.backgroundSprite.getWidth()
				/ Assets.backgroundSprite.getHeight()) {
			camera.viewportWidth = Assets.backgroundSprite.getWidth();
			camera.viewportHeight = camera.viewportWidth * height
					/ (float) width;
		} else {
			camera.viewportHeight = Assets.backgroundSprite.getHeight();
			camera.viewportWidth = camera.viewportHeight * width
					/ (float) height;
		}

		camera.position.x = camera.viewportWidth / 2;
		camera.position.y = camera.viewportHeight / 2;
		camera.update();

		batch.setProjectionMatrix(camera.combined);

		stage.getViewport().update(width, height, true);
		screenTable.getCell(table).width(width / 1.618034F);
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
		stage.dispose();
		batch.dispose();
	}
}
