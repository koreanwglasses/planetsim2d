package com.particlesim.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.math.Rectangle;
import com.particlesim.physics.math.Vector2;
import com.particlesim.errorHandling.BadVersionException;
import com.particlesim.graphics.ParticleBoxRenderer;

public class ParticleSim2D {

	public boolean debug = false;

	public static final String version = "0.2.1.2";
	public static final String[] compatibleVersions = new String[] { version,
			"0.1.2.2", "0.1.2.3", "0.2.0.0", "0.2.1.0", "0.2.1.1" };

	List<Particle> particleBox;

	public List<Particle> getParticleBox() {
		return particleBox;
	}

	public Map<String, Particle> nameIndex;

	public ParticleBoxRenderer prender;
	ParticlePhysics physics;

	ParticleUpdateListener updateListener;
	public boolean existsListener;

	public double timeScale = 1;

	public float minMass = 1000000;
	public float maxMass = 1001;

	public float minRadius = 10;
	public float maxRadius = 20;

	public float rangeX = 1000;
	public float rangeY = 1000;

	public float maxVel = 100;
	public float minVel = 0;

	public static float defaultMinMass = 1000000;
	public static float defaultMaxMass = 1001;

	public static float defaultMinRadius = 10;
	public static float defaultMaxRadius = 20;

	public static float defaultRangeX = 1000;
	public static float defaultRangeY = 1000;

	public static float defaultMaxVel = 100;
	public static float defaultMinVel = 0;

	public boolean bounded = false;
	public Rectangle bounds = new Rectangle(-1000, -1000, 3000, 3000);

	public boolean showVectors = false;

	private boolean initialized;

	public ParticleSim2D() {
		particleBox = new ArrayList<Particle>();
		nameIndex = new HashMap<String, Particle>();

		minMass = defaultMinMass;
		maxMass = defaultMaxMass;
		minRadius = defaultMinRadius;
		maxRadius = defaultMaxRadius;
		rangeX = defaultRangeX;
		rangeY = defaultRangeY;
		minVel = defaultMinVel;
		maxVel = defaultMaxVel;

		existsListener = false;

		initialized = false;
	}

	public void initialize() {
		prender = new ParticleBoxRenderer(this);
		physics = new ParticlePhysics(this);

		// Render first frame
		prender.firstFrame();

		initialized = true;

		resetSubFrames();
	}

	/**
	 * Randomly fills the simulation with particleCount particles.
	 * 
	 * @param particleCount
	 *            The numbers of particles to generate
	 */
	public void RGen(int particleCount) {
		Random rand = new Random();
		for (int c = 0; c < particleCount; c++) {
			Particle particle = new Particle(this, String.valueOf(c));
			particle.mass = (maxMass - minMass) * rand.nextFloat() + minMass;
			particle.radius = (maxRadius - minRadius) * rand.nextFloat()
					+ minRadius;
			particle.position = new Vector2(rand.nextFloat() * rangeX,
					rand.nextFloat() * rangeY);
			particle.velocity = new Vector2((maxVel - minVel)
					* rand.nextFloat() + minVel, (maxVel - minVel)
					* rand.nextFloat() + minVel);
			particleBox.add(particle);
		}
	}

	public void reset() {
		particleBox.clear();
		prender.firstFrame();
	}

	public boolean isPaused = true;

	public static final float frameInterval = .015F;
	float frame = 0;
	int update = 0;

	public float loop(float deltaTime) {
		if (initialized) {
			if (isPaused) {
				render(deltaTime);
				return deltaTime;
			}

			update(deltaTime);
			update++;

			frame += deltaTime;

			if (frame > frameInterval) {
				render(frame);
				if (debug)
					System.out.println(update + ":" + frame);
				float tempFrame = frame;
				frame = 0;
				update = 0;

				return tempFrame;
			}

			return -1;
		}
		return deltaTime;
	}

	int subFrames;
	
	public void resetSubFrames() {
		subFrames = 1;
	}

	public void smartLoop(float deltaTime) {
		if (!initialized)
			return;
		if (isPaused) {
			render(deltaTime);
			physics.update();
			return;
		}

		subFrames = (int) (subFrames * (frameInterval / deltaTime));
	//	if(subFrames > 100000) subFrames = 100000;
		if (subFrames < 1)
			subFrames = 1;

		double activeDelta = deltaTime / (double) subFrames;
		for (int count = 0; count < subFrames; count++) {
			update(activeDelta);
		}
		render(deltaTime);
		if (debug)
			System.out.println(subFrames + ":" + deltaTime);
	}

	public void render(float cameraTime) {
		prender.render(cameraTime);
	}

	public void update(double deltaTime) {
		physics.update();
		for (Particle particle : particleBox)
			particle.update(Math.min(deltaTime, 0.03F) * timeScale);
	}

	public void setUpdateListener(ParticleUpdateListener listener) {
		updateListener = listener;
		existsListener = listener != null;
	}

	public void add(Vector2 position, String name) {
		Random rand = new Random();
		Particle particle = new Particle(this, name);
		particle.mass = (maxMass - minMass) * rand.nextFloat() + minMass;
		particle.radius = (maxRadius - minRadius) * rand.nextFloat()
				+ minRadius;
		particle.position = position;
		particle.velocity = new Vector2((maxVel - minVel) * rand.nextFloat()
				+ minVel, (maxVel - minVel) * rand.nextFloat() + minVel);
		particleBox.add(particle);
		nameIndex.put(name, particle);
	}

	public void add(Vector2 position, Vector2 velocity, String name) {
		Random rand = new Random();
		Particle particle = new Particle(this, name);
		particle.mass = (maxMass - minMass) * rand.nextFloat() + minMass;
		particle.radius = (maxRadius - minRadius) * rand.nextFloat()
				+ minRadius;
		particle.position = position;
		particle.velocity = velocity;
		particleBox.add(particle);
		nameIndex.put(name, particle);
	}

	public Particle particleAt(Vector2 point) {
		for (Particle particle : particleBox)
			if (particle.position.dst2(point) <= particle.radius
					* particle.radius)
				return particle;
		return null;
	}

	public void add(Particle particle) {
		particleBox.add(particle);
		nameIndex.put(particle.name, particle);
	}

	public void addOrbit(Particle center, Particle particle, boolean clockwise) {
		Vector2 baseVel = particle.position.cpy().sub(center.position).nor();
		double dst = particle.position.dst(center.position);

		particle.velocity = baseVel.rotate90(clockwise ? -1 : 1)
				.scl(Math.sqrt((center.mass + particle.mass) / dst))
				.add(center.velocity);
		add(particle);
	}

	public Particle getParticle(String name) {
		for (Particle p : particleBox)
			if (p.name.equals(name))
				return p;
		return null;
	}

	public void remove(Particle particle) {
		particleBox.remove(particle);
		nameIndex.remove(particle);
	}

	public String ToString() {
		String output = "<version>" + version + "</version>";
		output += "<Particles>";
		for (Particle particle : particleBox) {
			output += particle.saveAsString();
			output += ";";
		}
		output += "</Particles>";
		output += "<render>" + timeScale + "</render>";
		return output;
	}

	public void LoadParse(String in) throws BadVersionException {
		parseInto(in);
		initialize();
	}

	public void parseInto(String in) throws BadVersionException {
		String versionString = in.substring(in.indexOf("<version>") + 9,
				in.indexOf("</version>")).replaceAll("\\s+", "");
		if (!isCompatible(versionString)) {
			throw new BadVersionException("Bad version!: File version - v"
					+ versionString + ", Required version - v" + version);
		}
		String particlesString = in.substring(in.indexOf("<Particles>") + 10,
				in.indexOf("</Particles>"));

		String[] particleStrings = particlesString.split(";");
		List<Particle> particles = new ArrayList<Particle>();
		for (String s : particleStrings) {
			particles.add(Particle.Parse(s, this));
		}

		if (in.contains("<render>")) {
			String renderProperties = in.substring(in.indexOf("<render>") + 8,
					in.indexOf("</render>"));
			timeScale = Double.parseDouble(renderProperties);
		}

		particleBox = particles;
		resetSubFrames();

		physics = new ParticlePhysics(this);
		initialized = true;
	}

	boolean isCompatible(String in) {
		for (String v : compatibleVersions)
			if (in.equals(v))
				return true;
		return false;
	}
}
