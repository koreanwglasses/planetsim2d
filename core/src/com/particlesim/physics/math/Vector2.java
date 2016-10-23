package com.particlesim.physics.math;

public class Vector2 {
	public double x;
	public double y;

	public Vector2() {
	}

	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2(Vector2 v) {
		x = v.x;
		y = v.y;
	}

	public Vector2(com.badlogic.gdx.math.Vector2 v) {
		x = v.x;
		y = v.y;
	}

	public Vector2 set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vector2 add(Vector2 v) {
		x += v.x;
		y += v.y;
		return this;
	}

	public Vector2 sub(Vector2 v) {
		x -= v.x;
		y -= v.y;
		return this;
	}

	public Vector2 scl(double scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}

	public Vector2 scl(float scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}

	public Vector2 nor() {
		double len = len();
		x /= len;
		y /= len;
		return this;
	}

	public double len() {
		return Math.sqrt(x * x + y * y);
	}

	public double len2() {
		return x * x + y * y;
	}

	public double dst(Vector2 v) {
		return Math.sqrt((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y));
	}
	
	public double dst2(Vector2 v) {
		return (x - v.x) * (x - v.x) + (y - v.y) * (y - v.y);
	}

	public double dot(Vector2 v) {
		return x * v.x + y * v.y;
	}

	public Vector2 cpy() {
		Vector2 copy = new Vector2(this);
		return copy;
	}

	public Vector2 rotate(double degrees) {
		double tempY = y;
		double m = len();
		y = y * Math.cos(-degrees) + m * Math.sqrt(1 - (y * y / (m * m)))
				* Math.sin(-degrees);
		x = Math.sqrt(m * m - tempY * tempY);
		return this;
	}

	
	public com.badlogic.gdx.math.Vector2 toFloat() {
		return new com.badlogic.gdx.math.Vector2((float) x, (float) y);
	}

	public Vector2 rotate90(int i) {
		double tempY = y;
		y = -x * i;
		x = tempY * i;
		return this;
	}

}
