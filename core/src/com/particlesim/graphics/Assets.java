package com.particlesim.graphics;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;

public class Assets {

	public static AssetManager manager;

	public static int screenX;
	public static int screenY;

	public static Texture backgroundTexture;
	public static Sprite backgroundSprite;

	public static Skin defaultSkin;

	public static Skin transparentSkin;

	public static void initialize() {
		TextureRegion temp1 = ScreenUtils.getFrameBufferTexture();
		screenX = temp1.getRegionWidth();
		screenY = temp1.getRegionHeight();
		
		manager = new AssetManager();
		manager.load("GUI/background.jpg", Texture.class);
		manager.load("GUI/Default Skin/uiskin.json",Skin.class);
		manager.load("GUI/Transparent Skin/uiskin.json",Skin.class);
	}

	public static void load() {

		backgroundTexture = manager.get("GUI/background.jpg", Texture.class);
		backgroundSprite = new Sprite(backgroundTexture);

		defaultSkin = manager.get("GUI/Default Skin/uiskin.json",Skin.class);

		transparentSkin = manager.get("GUI/Transparent Skin/uiskin.json",Skin.class);
		
	/*	try {
			CUDAPhysics.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
	}
}
