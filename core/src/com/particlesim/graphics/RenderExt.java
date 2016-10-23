package com.particlesim.graphics;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.particlesim.physics.math.Vector2;

public class RenderExt {
	public static void drawVector(ShapeRenderer sr, Vector2 position, Vector2 vector, float width) {
		Vector2 endPoint = position.cpy().add(vector); 
		sr.rectLine(position.toFloat(), endPoint.toFloat(), width);
		//sr.rectLine(endPoint.toFloat(), vector.cpy().rotate(150).scl(0.1F).add(endPoint).toFloat(), width);
	}
}
