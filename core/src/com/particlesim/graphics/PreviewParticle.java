package com.particlesim.graphics;

import com.particlesim.physics.Particle;
import com.particlesim.physics.ParticleSim2D;

public class PreviewParticle extends Particle {
	
	public PreviewParticle(ParticleSim2D parent, String name) {
		super(parent, name);
		forceShowVectors = true;
	}

	public PreviewParticle(PreviewParticle preview) {
		super(preview);
	}

	@Override
	public void update(double deltatime) {
		
	}
	
	public Particle playParticle() {
		return new Particle(this);
	}
}
