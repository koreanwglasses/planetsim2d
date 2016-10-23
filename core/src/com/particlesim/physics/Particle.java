package com.particlesim.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.particlesim.physics.math.Vector2;
import com.particlesim.graphics.PreviewParticle;
import com.particlesim.graphics.RenderExt;
import com.particlesim.graphics.RenderModes;

public class Particle {

	public static final Color defaultColor = Color.CYAN;

	public double mass;
	public double radius;
	public Vector2 position;
	public Vector2 velocity;
	public Vector2 force;
	public Color color;

	public String name;

	public ParticleSim2D parent;

	public boolean forceShowVectors = false;

	public Particle(String name) {
		this.name = name;
		color = defaultColor;
		force = new Vector2(0, 0);
	}

	public Particle(ParticleSim2D parent, String name) {
		this.name = name;
		color = defaultColor;
		force = new Vector2(0, 0);
		this.parent = parent;
	}

	public Particle(Particle particle) {
		name = particle.name;
		mass = particle.mass;
		radius = particle.radius;
		position = new Vector2(particle.position);
		velocity = new Vector2(particle.velocity);
		force = new Vector2(particle.force);
		color = new Color(particle.color);
		parent = particle.parent;
	}

	public Particle(PreviewParticle preview) {
		name = preview.name;
		mass = preview.mass;
		radius = preview.radius;
		position = preview.position;
		velocity = preview.velocity;
		force = preview.force;
		color = preview.color;
		parent = preview.parent;
		forceShowVectors = false;
	}

	public void render(ShapeRenderer sr, OrthographicCamera camera) {
		float screenPixel = camera.viewportHeight / Gdx.graphics.getHeight();
		sr.setColor(color);
		sr.circle((float) position.x, (float) position.y,
				(float) Math.max(radius, 2 * screenPixel));

		if (parent.showVectors || forceShowVectors) {
			if (parent.prender.followGrid
					&& parent.prender.renderMode == RenderModes.FOCUS) {
				sr.setColor(Color.YELLOW);
				RenderExt.drawVector(sr, position,
						velocity.cpy().sub(parent.prender.focus.velocity),
						Math.max(1.5F, 1.5F * screenPixel));
				sr.setColor(Color.GREEN);
				RenderExt.drawVector(sr, position, force.cpy().scl(.000001F),
						Math.max(1.5F, 1.5F * screenPixel));
			} else {
				sr.setColor(Color.YELLOW);
				RenderExt.drawVector(sr, position, velocity,
						Math.max(1.5F, 1.5F * screenPixel));
				sr.setColor(Color.GREEN);
				RenderExt.drawVector(sr, position, force.cpy().scl(.000001F),
						Math.max(1.5F, 1.5F * screenPixel));
			}
		}
	}

	public void update(double deltaTime) {
		velocity.add(force.cpy().scl(deltaTime / mass));

		if (parent != null && parent.bounded) {
			if (position.x < parent.bounds.x + radius) {
				velocity.x *= -1;
				position.x = parent.bounds.x + radius;
			} else if (position.x > parent.bounds.x + parent.bounds.width
					- radius) {
				velocity.x *= -1;
				position.x = parent.bounds.x + parent.bounds.width - radius;
			}
			if (position.y < parent.bounds.y + radius) {
				velocity.y *= -1;
				position.y = parent.bounds.y + radius;
			} else if (position.y > parent.bounds.y + parent.bounds.height
					- radius) {
				velocity.y *= -1;
				position.y = parent.bounds.y + parent.bounds.height - radius;
			}
		}

		position.add(velocity.cpy().scl(deltaTime));
	}

	@Override
	public String toString() {
		return name;
	}

	public static Particle combine(Particle A, Particle B) {
		boolean APriority = A.hasPriorityOver(B);
		Particle returnParticle = new Particle(A.parent, APriority ? A.name
				: B.name);
		returnParticle.mass = A.mass + B.mass;
		returnParticle.radius = Math.sqrt(A.radius * A.radius + B.radius
				* B.radius);
		returnParticle.position = APriority ? A.position : B.position;
		returnParticle.velocity = A.velocity.cpy().scl(A.mass)
				.add(B.velocity.cpy().scl(B.mass)).scl(1 / (A.mass + B.mass));
		returnParticle.color = APriority ? A.color : B.color;
		return returnParticle;
	}

	public boolean hasPriorityOver(Particle particle) {
		return this.radius > particle.radius;
	}

	public String saveAsString() {
		return "[" + name + "," + mass + "," + radius + "," + position.x + ","
				+ position.y + "," + velocity.x + "," + velocity.y + ","
				+ force.x + "," + force.y + "," + color.toString() + "]";
	}

	public static Particle Parse(String in, ParticleSim2D parent) {
		in = in.replaceAll("\\s+", "");

		int indexOfOpenBracket = in.indexOf("[");
		int indexOfLastBracket = in.lastIndexOf("]");

		in = in.substring(indexOfOpenBracket + 1, indexOfLastBracket);

		String[] args = in.split(",");

		Particle newParticle = new Particle(parent, args[0]);
		newParticle.mass = Float.parseFloat(args[1]);
		newParticle.radius = Float.parseFloat(args[2]);
		newParticle.position = new Vector2(Float.parseFloat(args[3]),
				Float.parseFloat(args[4]));
		newParticle.velocity = new Vector2(Float.parseFloat(args[5]),
				Float.parseFloat(args[6]));
		newParticle.force = new Vector2(Float.parseFloat(args[7]),
				Float.parseFloat(args[8]));
		if (args.length > 9) {
			if (args[9].equals("ffffffff"))
				newParticle.color = Color.WHITE;
			else
				newParticle.color = new Color((int) Long.parseLong(args[9], 16));
		}

		return newParticle;
	}
}
