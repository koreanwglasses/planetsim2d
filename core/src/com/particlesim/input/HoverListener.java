package com.particlesim.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class HoverListener extends ClickListener {
	private boolean temp = false;

	@Override
	public void enter(InputEvent event, float x, float y, int pointer,
			Actor fromActor) {
		SimulationUI.hoverExclusion = true;
	}

	@Override
	public void exit(InputEvent event, float x, float y, int pointer,
			Actor toActor) {
		if (temp) {
			temp = false;
			return;
		}
		SimulationUI.hoverExclusion = false;
	}

	@Override
	public void clicked(InputEvent event, float x, float y) {
		temp = true;
	}
}