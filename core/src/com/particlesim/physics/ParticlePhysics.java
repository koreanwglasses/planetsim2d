package com.particlesim.physics;

import java.util.ArrayList;
import java.util.List;

import com.particlesim.physics.math.Vector2;

public class ParticlePhysics {

	// public static double lengthScale = 1e-6;
	// public static double G = 6.6673e-11 * Math.pow(lengthScale,3);
	double G = 1;

	ParticleSim2D psim;
	public static boolean collisionsOn = true;

	public ParticlePhysics(ParticleSim2D psim) {
		this.psim = psim;
	}

	public void update() {
		// Avoid concurrency
		List<Particle> particleBox = psim.getParticleBox();
		collisions(particleBox);
		gravity(particleBox);
	}

	List<Particle> newParticles = new ArrayList<Particle>();

	void gravity(List<Particle> particleBox) {
		for (Particle particleA : particleBox) {
			Vector2 force = new Vector2(0, 0);
			for (Particle particleB : particleBox) {
				if (particleA != particleB) {
					double dist2 = particleA.position.dst2(particleB.position);
                    double dist = Math.sqrt(dist2);
					force.add(particleB.position.cpy().sub(particleA.position)
							.nor()
							.scl(G * particleB.mass * particleA.mass / dist2));
//                            .scl(100000 * particleB.mass * Math.log(dist - particleB.radius) / (dist2 * Math.log(dist))));
				}
			}
			particleA.force = force;
		}
	}

	void collisions(List<Particle> particleBox) {
		List<Particle> particles = new ArrayList<Particle>(particleBox);
		
		checkForCollision(particles, particles, particleBox);

		if (collisionsOn)
			while (newParticles.size() > 0) {
				particles = new ArrayList<Particle>(particleBox);
				List<Particle> tempNewParticles = new ArrayList<Particle>(
						newParticles);
				newParticles.clear();
				checkForCollision(tempNewParticles, particles, particleBox);
			}
	}

	List<Particle> checked;

	void checkForCollision(List<Particle> A, List<Particle> B,
			List<Particle> particleBox) {
//		if (!collisionsOn)
//			checked = new ArrayList<Particle>();
        for (Particle particleA : A) {
            for (Particle particleB : B) {
                if (particleBox.contains(particleA)
                        && particleBox.contains(particleB)
                        && particleA != particleB) {
                    double dist2 = particleA.position.dst2(particleB.position);
                    boolean isColliding = dist2 < (particleA.radius + particleB.radius)
                            * (particleA.radius + particleB.radius);
                    if (isColliding) {
                        if (collisionsOn) {
                            Particle newParticle = Particle.combine(particleA, particleB);
                            particleBox.remove(particleA);
                            psim.nameIndex.remove(particleA);
                            particleBox.remove(particleB);
                            psim.nameIndex.remove(particleB);
                            particleBox.add(newParticle);
                            psim.nameIndex.put(newParticle.name, newParticle);
                            newParticles.add(newParticle);
                            if (psim.existsListener) psim.updateListener.collisions(particleA, particleB, newParticle);
                        } else {

                        }
//						if (!checked.contains(particleB)
//								&& !checked.contains(particleA)) {
//							Vector2 mvA = particleA.velocity.cpy().scl(
//									particleA.mass);
//							Vector2 mvB = particleB.velocity.cpy().scl(
//									particleB.mass);
//							double tempForceMag = VectorExt.oppVectorLen(
//									mvA,
//									particleB.position.cpy().sub(
//											particleA.position))
//									+ VectorExt.oppVectorLen(
//											mvB,
//											particleA.position.cpy().sub(
//													particleB.position));
//							Vector2 velA = mvA
//									.cpy()
//									.sub(particleB.position.cpy()
//											.sub(particleA.position).nor()
//											.scl(tempForceMag))
//									.scl(particleA.mass);
//							particleB.velocity = mvB
//									.cpy()
//									.sub(particleA.position.cpy()
//											.sub(particleB.position).nor()
//											.scl(tempForceMag))
//									.scl(particleB.mass);
//							particleA.velocity = velA;
//							checked.add(particleB);
//							checked.add(particleA);
//						}
                    }
                }
            }
        }
	}
}
