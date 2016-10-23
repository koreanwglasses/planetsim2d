package com.particlesim.errorHandling;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ErrorDialog {

	Stage stage;
	Skin skin;

	public ErrorDialog(Stage stage, Skin skin) {
		this.stage = stage;
		this.skin = skin;
	}

	public void showErrorDialog(Exception error, String title) {
		final String message = error.getMessage();
		new Dialog(title, skin) {
			{
				text(message);
				button("OK");
			}
		}.show(stage);
	}
	
	public static void showErrorDialog(Exception error, String title,
			Stage stage, Skin skin) {
		final String message = error.getMessage();
		new Dialog(title, skin) {
			{
				text(message);
				button("OK");
			}
		}.show(stage);
	}
}
