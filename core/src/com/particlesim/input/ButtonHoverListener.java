package com.particlesim.input;

import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class ButtonHoverListener extends HoverListener{
	@Override
	public void clicked(InputEvent event, float x, float y) {
		super.clicked(event,x,y);
		SimulationUI.stage.setKeyboardFocus(null);
	}
}
