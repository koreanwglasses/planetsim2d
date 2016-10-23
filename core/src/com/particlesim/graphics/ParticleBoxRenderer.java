package com.particlesim.graphics;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.particlesim.physics.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.particlesim.physics.Particle;
import com.particlesim.physics.ParticleSim2D;

public class ParticleBoxRenderer {
	ParticleSim2D particlesim;
	public OrthographicCamera camera;
	ShapeRenderer sr;

	public float zoom;

	public Vector2 center;

	public static float marginFraction = 1.2F;

	public static RenderModes defaultRenderMode = RenderModes.AUTO;

	public RenderModes renderMode;
	public boolean followGrid = true;
	public Particle focus;

	public boolean createParticle = false;
	private PreviewParticle staticPreviewParticle;

	public PreviewParticle getPreviewParticle() {
		return previewParticle;
	}

	public void setPreviewParticle(PreviewParticle previewParticle) {
		this.previewParticle = previewParticle;
		this.staticPreviewParticle = new PreviewParticle(previewParticle);
	}

	private PreviewParticle previewParticle;

	public ParticleBoxRenderer(ParticleSim2D particlesim) {
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();

		renderMode = defaultRenderMode;

		this.particlesim = particlesim;

		// Initialise Camera & ShapeRenderer
		camera = new OrthographicCamera();
		camera.setToOrtho(false, width, height);
		sr = new ShapeRenderer();

		setZoom(0);
	}

	public void firstFrame() {
		if (renderMode == RenderModes.AUTO) {
			float aspectRatio = Gdx.graphics.getWidth()
					/ (float) Gdx.graphics.getHeight();

			double minX = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;

			double minY = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;

			if (particlesim.getParticleBox().isEmpty()) {
				minX = particlesim.rangeX / -2D;
				maxX = particlesim.rangeX / 2D;
				minY = particlesim.rangeY / -2D;
				maxY = particlesim.rangeY / 2D;
			} else
				for (Particle particle : particlesim.getParticleBox()) {
					if (particle.position.x < minX)
						minX = particle.position.x;
					if (particle.position.x > maxX)
						maxX = particle.position.x;
					if (particle.position.y < minY)
						minY = particle.position.y;
					if (particle.position.y > maxY)
						maxY = particle.position.y;
				}

			float targetWidth;
			float targetHeight;
			// Wider
			if ((maxY - minY) * aspectRatio <= maxX - minX) {
				targetWidth = (float) (maxX - minX) * marginFraction;
				targetHeight = targetWidth / aspectRatio;
			} else {
				// Taller
				targetHeight = (float) (maxY - minY) * marginFraction;
				targetWidth = targetHeight * aspectRatio;
			}
			if (targetWidth < minHeight * aspectRatio
					|| targetHeight < minHeight) {
				targetHeight = minHeight;
				targetWidth = minHeight * aspectRatio;
			}

			camera.viewportWidth = targetWidth;
			camera.viewportHeight = targetHeight;

			camera.position.x = (float) (maxX + minX) * 0.5F;
			camera.position.y = (float) (maxY + minY) * 0.5F;

			tempAR = Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();

			viewFrame = new Vector2(targetWidth, targetHeight);

			center = new Vector2((maxX + minX) * 0.5F, (maxY + minY) * 0.5F);

		} else {
			com.badlogic.gdx.math.Vector2 pos = new com.badlogic.gdx.math.Vector2();
			particlesim.bounds.getCenter(pos);
			camera.position.x = pos.x;
			camera.position.y = pos.y;
			center = new Vector2(pos.x, pos.y);

			camera.viewportHeight = particlesim.bounds.height * marginFraction;
			camera.viewportWidth = camera.viewportHeight
					* Gdx.graphics.getWidth()
					/ (float) Gdx.graphics.getHeight();
			viewFrame = new Vector2(camera.viewportWidth, camera.viewportHeight);
		}
	}

	public void render(float cameraTime) {
		List<Particle> particles = particlesim.getParticleBox();

		// Clear screen & set background to black
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Keep everything on screen

		// Get some screen info
		aspectRatio = Gdx.graphics.getWidth()
				/ (float) Gdx.graphics.getHeight();
		ARChanged = tempAR != aspectRatio;

		if (ARChanged) {
			if (aspectRatio > tempAR)
				camera.viewportWidth = camera.viewportHeight * aspectRatio;
			else
				camera.viewportHeight = camera.viewportWidth / aspectRatio;
			viewFrame = new Vector2(camera.viewportWidth / zoom,
					camera.viewportHeight / zoom);
		}

		// Update the camera
		switch (renderMode) {
		case AUTO:
			autoZoom(particles);
			updateZoom(cameraTime);
			updatePosition(cameraTime);
			break;
		case MANUAL:
			updateZoom(cameraTime);
			updatePosition(cameraTime);
			break;
		case FOCUS:
			center = focus.position;
			updateZoom(cameraTime);
			camera.position.x = (float) center.x;
			camera.position.y = (float) center.y;
		}
		camera.update();
		sr.setProjectionMatrix(camera.combined);

		// Update creation of particle if neccessary
		if(renderMode == RenderModes.FOCUS && followGrid)
			previewParticle.position = new Vector2(staticPreviewParticle.position.x - camera.position.x, staticPreviewParticle.position.y - camera.position.y);

		// Render particles one by one
		sr.begin(ShapeType.Filled);
		checkeredBG();
		for (Particle particle : particles) {
			particle.render(sr, camera);
		}
		if (createParticle) {
			previewParticle.render(sr, camera);
		}
		sr.end();
		if (particlesim.bounded) {
			sr.begin(ShapeType.Line);
			sr.setColor(1, 1, 1, 1);
			sr.rect(particlesim.bounds.x, particlesim.bounds.y,
					particlesim.bounds.width, particlesim.bounds.height);
			sr.end();
		}

		tempAR = aspectRatio;
	}

	public static float minHeight = 300;

	public static float positionLerpSpeed = .5F;
	public static float zoomLerpSpeed = .5F;

	private float aspectRatio;
	private float tempAR = 0;
	private boolean ARChanged;

	public Vector2 viewFrame;

	private void autoZoom(List<Particle> particles) {
		if (particles.isEmpty())
			return;
		Particle initParticle = particles.get(0);
		double minX = initParticle.position.x;
		double maxX = initParticle.position.x;

		double minY = initParticle.position.y;
		double maxY = initParticle.position.y;

		for (Particle particle : particles) {
			if (particle.position.x - particle.radius < minX)
				minX = particle.position.x - particle.radius;
			if (particle.position.x + particle.radius > maxX)
				maxX = particle.position.x + particle.radius;
			if (particle.position.y - particle.radius < minY)
				minY = particle.position.y - particle.radius;
			if (particle.position.y + particle.radius > maxY)
				maxY = particle.position.y + particle.radius;
		}

		float targetWidth;
		float targetHeight;
		// Wider
		if ((maxY - minY) * aspectRatio <= maxX - minX) {
			targetWidth = (float) (maxX - minX) * marginFraction;
			targetHeight = targetWidth / aspectRatio;
		} else {
			// Taller
			targetHeight = (float) (maxY - minY) * marginFraction;
			targetWidth = targetHeight * aspectRatio;
		}
		if (targetWidth < minHeight * aspectRatio || targetHeight < minHeight) {
			targetHeight = minHeight;
			targetWidth = minHeight * aspectRatio;
		}

		viewFrame = new Vector2(targetWidth, targetHeight);

		center = new Vector2((maxX + minX) * 0.5d, (maxY + minY) * 0.5d);
	}

	public static float checkerWidth = .5F;
	float activeCheckerWidth;
	int zoomActiveCheckerWidth;

	public void setZoom(float zoom) {
		activeZoom = Math.pow(1.5, -zoom);
		this.zoom = zoom;
	}

	public static final double eighth = Math.pow(1.5, 6);

	public void updateChkr(float base) {
		int e = 0;
		while (base > eighth) {
			e++;
			base /= eighth;
		}
		activeCheckerWidth = (float) (checkerWidth * Math.pow(eighth, e));
	}

	public float getZoom() {
		return zoom;
	}

	
	static final Color GRAYISH= new Color(0x354045FF);
	
	private void checkeredBG() {
		updateChkr(Math.min(currentFrame.x, currentFrame.y));
		int xMin;
		int yMin;
		int xMax;
		int yMax;
		xMin = (int) Math
				.floor((camera.position.x - camera.viewportWidth * 0.5F)
						/ activeCheckerWidth);
		yMin = (int) Math
				.floor((camera.position.y - camera.viewportHeight * 0.5F)
						/ activeCheckerWidth);
		xMax = (int) Math
				.ceil((camera.position.x + camera.viewportWidth * 0.5F)
						/ activeCheckerWidth);
		yMax = (int) Math
				.ceil((camera.position.y + camera.viewportHeight * 0.5F)
						/ activeCheckerWidth);

		if (renderMode == RenderModes.FOCUS && followGrid) {
			float shiftX = ((float) (focus.position.x) % (activeCheckerWidth*2));
		//	if(shiftX < 0) shiftX += activeCheckerWidth;
			float shiftY = ((float) (focus.position.y) % (activeCheckerWidth*2));
		//	if(shiftY < 0) shiftY += activeCheckerWidth;
			for (int x = xMin - 2; x < xMax + 2; x++)
				for (int y = yMin - 2; y < yMax + 2; y++) {
					if ((((x & 1) | (y & 1)) != 1)
							|| (((x & 1) & (y & 1)) == 1)) {
						sr.setColor(GRAYISH);
						sr.rect(x * activeCheckerWidth + shiftX, y
								* activeCheckerWidth + shiftY,
								activeCheckerWidth, activeCheckerWidth);
					}
				}
		} else
			for (int x = xMin; x < xMax; x++)
				for (int y = yMin; y < yMax; y++) {
					if ((((x & 1) | (y & 1)) != 1)
							|| (((x & 1) & (y & 1)) == 1)) {
						sr.setColor(Color.DARK_GRAY);
						sr.rect(x * activeCheckerWidth, y * activeCheckerWidth,
								activeCheckerWidth, activeCheckerWidth);
					}
				}
	}

	double activeZoom;

	com.badlogic.gdx.math.Vector2 currentFrame;

	private void updateZoom(float cameraTime) {
		com.badlogic.gdx.math.Vector2 currentFrame = new com.badlogic.gdx.math.Vector2(
				camera.viewportWidth, camera.viewportHeight);
		currentFrame.interpolate(viewFrame.cpy().scl(activeZoom).toFloat(),
				cameraTime * zoomLerpSpeed, Interpolation.exp5Out);
		this.currentFrame = currentFrame;
		camera.viewportWidth = currentFrame.x;
		camera.viewportHeight = currentFrame.y;
	}

	private void updatePosition(float cameraTime) {
		camera.position.interpolate(new Vector3((float) center.x,
				(float) center.y, camera.position.z), positionLerpSpeed
				* cameraTime, Interpolation.exp5Out);
	}

	public void dispose() {
		sr.dispose();
	}
}
