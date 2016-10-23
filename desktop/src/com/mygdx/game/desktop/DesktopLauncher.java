package com.mygdx.game.desktop;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.particlesim.input.SimulationUI;
//import com.badlogic.gdx.backends.lwjgl.LwjglFrame;
import com.particlesim.main.Main;
import com.particlesim.physics.ParticleSim2D;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.samples = 2;
		config.width = 800; // 800
		config.height = 600; // 600
		config.vSyncEnabled = false;
		config.foregroundFPS = 0;
		config.backgroundFPS = 0;
		config.title = "Particle Sim 2D v" + ParticleSim2D.version;

		SimulationUI.debug = arg.length > 0 && arg[0].equalsIgnoreCase("debug");
		
		if (arg.length > 0 && !arg[0].equalsIgnoreCase("debug")) {
			File file = new File(arg[0]);
			if (isFileValid(file) && file.exists())
				{new LwjglApplication(new Main(file), config);
			return;}
		}

		new LwjglApplication(new Main(), config);
		// new LwjglFrame(new Main(), config).setVisible(true);
	}

	public static boolean isFileValid(File f) {
		try {
			f.getCanonicalPath();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
