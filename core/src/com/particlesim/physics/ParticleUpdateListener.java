package com.particlesim.physics;


public interface ParticleUpdateListener {
	void collisions(Particle A, Particle B, Particle newParticle);
}
