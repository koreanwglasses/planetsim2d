package com.particlesim.main;

import java.io.File;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.particlesim.input.SimulationUI;

public class Main extends Game {

	// TODO: Add momentum
	public Main() {}
	
	public Main(File file) {
		SimulationUI sim = new SimulationUI(this);
		sim.initialize(SimulationUI.STANDARD);
		sim.load(file);
		setScreen(sim);
	}

	@Override
	public void create() {
		Gdx.graphics.setVSync(true);
		setScreen(new LoadScreen(this));
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}
}
