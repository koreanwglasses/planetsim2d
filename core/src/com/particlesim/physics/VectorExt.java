package com.particlesim.physics;

import com.particlesim.physics.math.Vector2;

public class VectorExt {
	public static Vector2 oppVector(Vector2 u, Vector2 v) {
		return v.cpy().rotate90(1).scl(u.dot(v) / v.len2());
	}
	public static double oppVectorLen(Vector2 u, Vector2 v) {
		return Math.abs(u.dot(v) / v.len2());
	}
}
