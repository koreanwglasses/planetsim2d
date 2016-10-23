package com.particlesim.input;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.particlesim.errorHandling.BadVersionException;
import com.particlesim.errorHandling.ErrorDialog;
import com.particlesim.graphics.Assets;
import com.particlesim.graphics.ParticleBoxRenderer;
import com.particlesim.graphics.PreviewParticle;
import com.particlesim.graphics.RenderModes;
import com.particlesim.physics.Particle;
import com.particlesim.physics.ParticleSim2D;
import com.particlesim.physics.ParticleUpdateListener;
import com.particlesim.physics.math.Vector2;

public class SimulationUI implements Screen, InputProcessor {

	public static boolean suspend;
	public static boolean hoverExclusion;

	public static boolean debug;

	boolean nameSet;
	String name;

	boolean massSet;
	double mass;

	boolean radiusSet;
	double radius;

	boolean positionXSet;
	boolean positionYSet;
	Vector2 position;

	boolean velocityXSet;
	boolean velocityYSet;
	Vector2 velocity;

	boolean colorSet;
	Color color;

	public boolean isFullscreen;

	int windowedWidth;
	int windowedHeight;

	ErrorDialog ed;

	ParticleSim2D psim;
	static int startCount = 10;

	public static Stage stage;
	Viewport viewport;

	boolean isExpanded = false;

	Game game;

	// Particle focusParticle;

	public static final int STANDARD = 0;
	public static final int LOAD = 1;

	Slider hsbTimeScale;

	Label lblParticleName;
	Label lblParticleMass;
	Label lblParticleRadius;

	Label lblSetName;
	TextField txtSetName;

	Label lblSetMass;
	TextField txtSetMass;

	Label lblSetRadius;
	TextField txtSetRadius;

	Label lblSetPosition;
	TextField txtSetPositionX;
	TextField txtSetPositionY;

	Label lblSetVelocity;
	TextField txtSetVelocityX;
	TextField txtSetVelocityY;

	Label lblSetColor;
	TextField txtSetColor;

	boolean orbit;
	Particle focusParticle;
	TextButton btnCreate;
	boolean threadLockout1 = false;
	TextField txtSearch;
	static final int maxResultLength = 15;
	com.badlogic.gdx.scenes.scene2d.ui.List<Particle> lstResults;

	boolean wasDown = false;

	Preferences pref = Preferences.userRoot();
	JFileChooser chooser;

	public SimulationUI(Game game) {
		this.game = game;

		chooser = new JFileChooser() {

			@Override
			public void approveSelection() {
				File f = getSelectedFile();
				if (!f.getName().endsWith(".psim"))
					f = new File(f.getAbsoluteFile() + ".psim");
				if (f.exists() && getDialogType() == SAVE_DIALOG) {
					int result = JOptionPane.showConfirmDialog(this,
							"The file already exists, overwrite?", "Existing file",
							JOptionPane.YES_NO_CANCEL_OPTION);
					switch (result) {
					case JOptionPane.YES_OPTION:
						super.approveSelection();
						return;
					case JOptionPane.NO_OPTION:
						return;
					case JOptionPane.CLOSED_OPTION:
						return;
					case JOptionPane.CANCEL_OPTION:
						cancelSelection();
						return;
					}
				}
				super.approveSelection();
			}
		};
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Particle Sim Files", "psim");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(new File(pref.get("LAST_PATH", "")));
	}

	public boolean initialize(int mode) {
		// Initialization
		switch (mode) {
		case 0:
			psim = new ParticleSim2D();

			psim.minMass = 1000;
			psim.maxMass = 1000000;

			psim.minRadius = 5;
			psim.maxRadius = 10;

			psim.maxVel = 100;
			psim.minVel = -100;

			psim.rangeX = 1000;
			psim.rangeY = 1000;

			psim.bounded = false;
			psim.bounds = new Rectangle(0, 0, 1000, 1000);

			// setPaused(false);
			// psim.RGen(startCount);
			psim.initialize();
			break;

		case 1:
			psim = new ParticleSim2D();
			boolean successOut = load();
			if (!successOut)
				return false;

			break;
		default:
			assert false;
		}

		// Initialize
		position = new Vector2();
		velocity = new Vector2();

		psim.setUpdateListener(new ParticleUpdateListener() {
			@Override
			public void collisions(Particle A, Particle B, Particle newParticle) {
				_collisions(A, B, newParticle);
			}

		});
		psim.debug = debug;

		// HUD
		stage = new Stage();
		ed = new ErrorDialog(stage, Assets.defaultSkin);

		InputMultiplexer inputMulti = new InputMultiplexer();
		inputMulti.addProcessor(stage);
		inputMulti.addProcessor(this);

		Gdx.input.setInputProcessor(inputMulti);

		viewport = new ScreenViewport();
		stage.setViewport(viewport);

		Table screenTable = new Table();
		screenTable.setFillParent(true);
		stage.addActor(screenTable);

		Table leftSide = new Table();

		final Table headerTable = new Table();

		final Table particleInfoTable = new Table();
		// particleInfoTable.setDebug(true);

		final Table table = new Table();
		// table.setDebug(true);
		table.setVisible(isExpanded);

		final Label lblTitle = new Label("Simulation Mode",
				Assets.transparentSkin);
		final TextButton btnExpand = new TextButton("<<",
				Assets.transparentSkin);
		btnExpand.addListener(new HoverListener() {
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				isExpanded = !isExpanded;
				btnExpand.setText(isExpanded ? ">>" : "<<");
				table.setVisible(isExpanded);
			}
		});

		screenTable.add(lblTitle).left();
		screenTable.add(btnExpand).top().right().pad(2);
		screenTable.row();

		screenTable.add(leftSide).expand().left().fill();

		// Header
		// final Label lblSearch = new Label("Search", Assets.transparentSkin);
		txtSearch = new TextField("", Assets.transparentSkin);
		txtSearch.setMessageText("Search");

		lstResults = new com.badlogic.gdx.scenes.scene2d.ui.List<Particle>(
				Assets.transparentSkin);

		txtSearch.addListener(new HoverListener());
		txtSearch.addListener(new InputListener() {
			public boolean keyTyped(InputEvent event, char character) {
				new Thread() {
					public void run() {
						if (threadLockout1)
							return;
						threadLockout1 = true;
						// lblSearch.setText("Searching...");
						if (txtSearch.getText().isEmpty())
							lstResults.clearItems();
						else {
							Array<Particle> search = new Array<Particle>();
							String upperText = txtSearch.getText()
									.toUpperCase();
							for (Particle particle : psim.getParticleBox()) {
								if (particle.name.toUpperCase().startsWith(
										upperText)
										&& search.size < maxResultLength)
									search.add(particle);
							}
							lstResults.setItems(search);
						}
						lblTitle.setText("Simulation Mode "
								+ psim.getParticleBox().size());
						Gdx.graphics.requestRendering();
						threadLockout1 = false;
					}
				}.start();
				return false;
			}
		});

		lstResults.addListener(new ButtonHoverListener());

		TextButton btnFocusSearch = new TextButton("Focus",
				Assets.transparentSkin);
		btnFocusSearch.addListener(new ButtonHoverListener() {
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				Particle selected = lstResults.getSelected();
				if (selected != null) {
					setFocus(selected);
				}
			}
		});

		leftSide.add(headerTable).expand().fill();
		// headerTable.add(lblSearch).expandX().colspan(2).left().pad(2);
		// headerTable.row();
		headerTable.add(txtSearch).pad(2);
		headerTable.add(btnFocusSearch).expandX().pad(2).left();
		headerTable.row();
		headerTable.add(lstResults).expand().fillY().colspan(2).top().left()
				.pad(2);
		// End Header

		// Particle Info
		lblParticleName = new Label("Name:", Assets.transparentSkin);
		lblParticleMass = new Label("Mass:", Assets.transparentSkin);
		lblParticleRadius = new Label("Radius:", Assets.transparentSkin);

		leftSide.row();
		leftSide.add(particleInfoTable).expand().fill();
		particleInfoTable.add(lblParticleName).expand().bottom().left();
		particleInfoTable.row();
		particleInfoTable.add(lblParticleMass).bottom().left();
		particleInfoTable.row();
		particleInfoTable.add(lblParticleRadius).bottom().left();
		// End Particle Info

		// Controls
		Table subTableA = new Table();
		Table subTableB = new Table();

		TextButton btnSave = new TextButton("Save", Assets.transparentSkin);
		btnSave.addListener(new ButtonHoverListener() {
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				save();
			}
		});

		TextButton btnLoad = new TextButton("Load", Assets.transparentSkin);
		btnLoad.addListener(new ButtonHoverListener() {
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				load();
			}
		});

		TextButton btnPause = new TextButton("Pause", Assets.transparentSkin);
		btnPause.addListener(new ButtonHoverListener() {
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				togglePaused();
			}
		});

		TextButton btnReset = new TextButton("Reset", Assets.transparentSkin);
		btnReset.addListener(new ButtonHoverListener() {
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				reset();
			}
		});

		df = new DecimalFormat("0.00");
		lblTimeScale = new Label("0.00: 1.00X", Assets.transparentSkin);
		hsbTimeScale = new Slider(-2F, 1F, 0.01F, false, Assets.transparentSkin);
		if (mode == LOAD) {
			hsbTimeScale.setValue((float) Math.log10(psim.timeScale));
			updateHSB();
		} else
			hsbTimeScale.setValue(0);
		hsbTimeScale.addListener(new ButtonHoverListener() {
			public void touchDragged(InputEvent event, float x, float y,
					int pointer) {
				updateHSB();
			}

			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				updateHSB();
			}
		});

		lblSetName = new Label("Name*:", Assets.transparentSkin);
		txtSetName = new TextField("", Assets.transparentSkin);
		txtSetName.setMessageText(getName());
		txtSetName.addListener(new HoverListener());

		lblSetMass = new Label("Mass*:", Assets.transparentSkin);
		txtSetMass = new TextField("", Assets.transparentSkin);
		txtSetMass.addListener(new HoverListener());

		lblSetRadius = new Label("Radius*:", Assets.transparentSkin);
		txtSetRadius = new TextField("", Assets.transparentSkin);
		txtSetRadius.addListener(new HoverListener());

		lblSetPosition = new Label("Pos*:", Assets.transparentSkin);
		txtSetPositionX = new TextField("", Assets.transparentSkin);
		txtSetPositionX.addListener(new HoverListener());
		txtSetPositionY = new TextField("", Assets.transparentSkin);
		txtSetPositionY.addListener(new HoverListener());

		lblSetVelocity = new Label("Vel*:", Assets.transparentSkin);
		txtSetVelocityX = new TextField("", Assets.transparentSkin);
		txtSetVelocityX.addListener(new HoverListener());
		txtSetVelocityY = new TextField("", Assets.transparentSkin);
		txtSetVelocityY.addListener(new HoverListener());

		lblSetColor = new Label("Color*:", Assets.transparentSkin);
		txtSetColor = new TextField("", Assets.transparentSkin);
		txtSetColor.addListener(new HoverListener());

		CheckBox chkOrbit = new CheckBox("Orbit", Assets.transparentSkin);
		chkOrbit.addListener(new ButtonHoverListener() {
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				orbit = !orbit;
				txtSetVelocityX.setDisabled(orbit);
				txtSetVelocityY.setDisabled(orbit);
			}
		});

		TextButton btnOk = new TextButton("OK", Assets.transparentSkin);
		btnOk.addListener(new ButtonHoverListener() {
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				setNewParticleValues();
			}
		});

		btnCreate = new TextButton("Create", Assets.transparentSkin);
		btnCreate.setDisabled(true);
		btnCreate.addListener(new ButtonHoverListener() {
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				if (setNewParticleValues()) {
					Particle newParticle = new Particle(psim, getName());
					newParticle.mass = new Double(mass);
					newParticle.radius = new Double(radius);
					newParticle.position = new Vector2(position);
					if (orbit && focusParticle != null)
						psim.addOrbit(focusParticle, newParticle, true);
					else {
						newParticle.velocity = new Vector2(velocity);
						psim.add(newParticle);
					}
					newParticle.color = new Color(color);
					spawn(newParticle);
					nextName();
				}
			}
		});

		screenTable.add(table).expand().width(200).fillY().right();
		table.add(subTableA).colspan(2).expandX().right();
		subTableA.add(btnSave).expandX().fillX().pad(2);
		subTableA.add(btnLoad).expandX().fillX().pad(2);
		subTableA.row();
		subTableA.add(btnPause).expandX().fillX().pad(2);
		subTableA.add(btnReset).expandX().fillX().pad(2);
		table.row();
		table.add(lblTimeScale).expandX().right().colspan(2).pad(2);
		table.row();
		table.add(hsbTimeScale).width(150).expandX().right().colspan(2).pad(2);
		table.row();
		table.add(lblSetName).right().pad(2);
		table.add(txtSetName).pad(2).width(100);
		table.row();
		table.add(lblSetMass).right().pad(2);
		table.add(txtSetMass).pad(2).width(100);
		table.row();
		table.add(lblSetRadius).right().pad(2);
		table.add(txtSetRadius).pad(2).width(100);
		table.row();
		table.add(lblSetPosition).right().pad(2);
		table.add(txtSetPositionX).pad(2).width(100);
		table.row();
		table.add();
		table.add(txtSetPositionY).pad(2).width(100);
		table.row();
		table.add(lblSetVelocity).right().pad(2);
		table.add(txtSetVelocityX).pad(2).width(100);
		table.row();
		table.add(chkOrbit).right().pad(2);
		table.add(txtSetVelocityY).pad(2).width(100);
		table.row();
		table.add(lblSetColor).right().pad(2);
		table.add(txtSetColor).pad(2).width(100);
		table.row();
		table.add(subTableB).expand().colspan(2).right().top();
		subTableB.add(btnOk).expandX().fillX().pad(2);
		subTableB.add(btnCreate).expandX().fillX().pad(2);
		// End Controls

		return true;
	}

	public void _collisions(Particle A, Particle B, Particle newParticle) {
		Array<Particle> list = lstResults.getItems();
		boolean containsA = list.contains(A, true);
		boolean containsB = list.contains(B, true);
		if (containsA)
			list.removeValue(A, true);
		if (containsB)
			list.removeValue(B, true);
		if ((containsA && A.hasPriorityOver(B))
				|| (containsB && B.hasPriorityOver(A)))
			list.add(newParticle);

		if ((focusParticle == A && A.hasPriorityOver(B))
				|| (focusParticle == B && B.hasPriorityOver(A)))
			setFocus(newParticle, false);
	}

	public void spawn(Particle particle) {
		if (!txtSearch.getText().isEmpty()
				&& particle.name.toUpperCase().startsWith(
						txtSearch.getText().toUpperCase())) {
			Array<Particle> list = new Array<Particle>(lstResults.getItems());
			if (list.size < maxResultLength) {
				list.add(particle);
				lstResults.setItems(list);
			}
		}
	}

	public boolean setNewParticleValues() {
		if (txtSetName.getText() == ""
				|| psim.nameIndex.containsKey(txtSetName.getText())) {
			lblSetName.setText("Name*:");
			nameSet = false;
		} else {
			lblSetName.setText("Name:");
			nameSet = true;
			name = txtSetName.getText();
		}

		if (txtSetMass.getText() == "") {
			massSet = false;
			lblSetMass.setText("Mass*:");
		} else {
			try {
				mass = Double.parseDouble(txtSetMass.getText());
				massSet = true;
				lblSetMass.setText("Mass:");
			} catch (Exception e) {
				massSet = false;
				lblSetMass.setText("Mass*:");
			}
		}

		if (txtSetRadius.getText() == "") {
			radiusSet = false;
			lblSetRadius.setText("Radius*:");
		} else {
			try {
				radius = Double.parseDouble(txtSetRadius.getText());
				radiusSet = true;
				lblSetRadius.setText("Radius:");
			} catch (Exception e) {
				radiusSet = false;
				lblSetRadius.setText("Radius*:");
			}
		}

		if (txtSetPositionX.getText() == "")
			positionXSet = false;
		else {
			try {
				position.x = Double.parseDouble(txtSetPositionX.getText());
				positionXSet = true;
			} catch (Exception e) {
				positionXSet = false;
			}
		}
		if (txtSetPositionY.getText() == "")
			positionYSet = false;
		else {
			try {
				position.y = Double.parseDouble(txtSetPositionY.getText());
				positionYSet = true;
			} catch (Exception e) {
				positionYSet = false;
			}
		}
		if (positionXSet && positionYSet)
			lblSetPosition.setText("Pos:");
		else
			lblSetPosition.setText("Pos*:");

		if (txtSetVelocityX.getText() == "")
			velocityXSet = false;
		else {
			try {
				velocity.x = Double.parseDouble(txtSetVelocityX.getText());
				velocityXSet = true;
			} catch (Exception e) {
				velocityXSet = false;
			}
		}
		if (txtSetVelocityY.getText() == "")
			velocityYSet = false;
		else {
			try {
				velocity.y = Double.parseDouble(txtSetVelocityY.getText());
				velocityYSet = true;
			} catch (Exception e) {
				velocityYSet = false;
			}
		}

		if (velocityXSet && velocityYSet)
			lblSetVelocity.setText("Vel:");
		else
			lblSetVelocity.setText("Vel*:");

		if (txtSetColor.getText() == "") {
			colorSet = false;
			lblSetColor.setText("Color*:");
		} else {
			try {
				color = new Color(
						Integer.parseInt(txtSetColor.getText(), 16) * 256 + 0xFF);
				colorSet = true;
				lblSetColor.setText("Color:");
			} catch (Exception e) {
				colorSet = false;
				lblSetColor.setText("Color*:");
			}
		}

		boolean allSet = massSet && radiusSet && velocityXSet && velocityXSet
				&& positionXSet && positionXSet;
		btnCreate.setDisabled(!allSet);
		return allSet;
	}

	DecimalFormat df;
	Label lblTimeScale;

	private void updateHSB() {
		float value = hsbTimeScale.getValue();
		psim.timeScale = Math.pow(10, value);
		lblTimeScale.setText(df.format(value) + ": "
				+ df.format(psim.timeScale) + "X");
	}

	public void setFocus(Particle particle) {
		if (particle == null) {
			lblParticleName.setText("Name:");
			lblParticleMass.setText("Mass:");
			lblParticleRadius.setText("Radius:");

			psim.prender.renderMode = RenderModes.AUTO;
		} else {
			lblParticleName.setText("Name: " + particle.name);
			lblParticleMass.setText("Mass: " + particle.mass);
			lblParticleRadius.setText("Radius: " + particle.radius);

			focusParticle = particle;
			psim.prender.focus = particle;
			psim.prender.renderMode = RenderModes.FOCUS;
			rpFlag = false;
		}
	}

	public void setFocus(Particle particle, boolean force) {
		if (particle == null) {
			lblParticleName.setText("Name:");
			lblParticleMass.setText("Mass:");
			lblParticleRadius.setText("Radius:");

			psim.prender.renderMode = RenderModes.AUTO;
		} else {
			lblParticleName.setText("Name: " + particle.name);
			lblParticleMass.setText("Mass: " + particle.mass);
			lblParticleRadius.setText("Radius: " + particle.radius);

			focusParticle = particle;
			psim.prender.focus = particle;
			if (force)
				psim.prender.renderMode = RenderModes.FOCUS;
			rpFlag = false;
		}
	}

	public void setPaused(boolean isPaused) {
		Gdx.graphics.setVSync(isPaused);
		psim.isPaused = isPaused;
	}

	public void togglePaused() {
		psim.isPaused = !psim.isPaused;
		Gdx.graphics.setVSync(psim.isPaused);
	}

	@Override
	public void render(float delta) {
		/*
		 * float frame; if ((frame = psim.loop(delta))> - 1) {
		 * stage.act(Math.min(frame, 0.03F)); stage.draw(); }
		 */
		psim.smartLoop(delta);
		stage.act(Math.min(delta, 0.03F));
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void show() {

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	public void save() {
		suspend = true;
		setPaused(true);

		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("Saving as: "
					+ chooser.getSelectedFile().getName());
		}
		if (returnVal == JFileChooser.CANCEL_OPTION) {
			// setPaused(false);
			suspend = false;
			return;
		}

		String save = psim.ToString();

		try {

			File file = chooser.getSelectedFile();
			if (!file.getName().endsWith(".psim"))
				file = new File(file.getAbsoluteFile() + ".psim");
		/*	int similar = 0;
			while (file.exists()) {
				String fileTo = file.getAbsoluteFile().toString();
				file.renameTo(new File(fileTo.substring(0, fileTo.length() - 5)
						+ " (OLD" + similar + ").psim"));
				similar++;
			} */

			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(save);
			bw.close();

			System.out.println("Saved");

		} catch (IOException e) {
			e.printStackTrace();
		}

		suspend = false;
	}

	public void load(File file) {
		suspend = true;
		setPaused(true);
		try {
			FileReader fr = new FileReader(chooser.getSelectedFile()
					.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);

			String load = br.readLine();
			br.close();

			psim.LoadParse(load);
			if (hsbTimeScale != null) {
				hsbTimeScale.setValue((float) Math.log10(psim.timeScale));
				updateHSB();
			}
		} catch (BadVersionException e) {
			ed.showErrorDialog(e, "Bad Version!");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		suspend = false;
	}

	public boolean load() {

		suspend = true;
		setPaused(true);

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("Opening: "
					+ chooser.getSelectedFile().getName());
			pref.put("LAST_PATH", chooser.getSelectedFile().getPath());
		}
		if (returnVal == JFileChooser.CANCEL_OPTION) {
			suspend = false;
			return false;
		}

		try {
			FileReader fr = new FileReader(chooser.getSelectedFile()
					.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);

			String load = br.readLine();
			br.close();

			psim.LoadParse(load);
			if (hsbTimeScale != null) {
				hsbTimeScale.setValue((float) Math.log10(psim.timeScale));
				updateHSB();
			}
		} catch (BadVersionException e) {
			ed.showErrorDialog(e, "Bad Version!");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		suspend = false;
		return true;
	}

	public void reset() {
		setPaused(true);
		suspend = true;

		new Dialog("Resetting...", Assets.defaultSkin) {
			{
				text("You are about to delete all\nof the particles on the simulation.\nAny unsaved data will be lost.");
				button("Continue", true);
				button("Cancel", false);
			}

			@Override
			protected void result(Object object) {
				if (object == (Object) true) {
					unnamedCount = 0;
					nextName();
					txtSearch.setText("");
					psim.reset();
				}

				// setPaused(false);
				suspend = false;
			}

		}.show(stage);
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (suspend || stage.getKeyboardFocus() != null)
			return false;
		switch (keycode) {
		case Keys.EQUALS:
			psim.prender.setZoom(psim.prender.zoom + 1);
			break;
		case Keys.MINUS:
			psim.prender.setZoom(psim.prender.zoom - 1);
			break;
		case Keys.SPACE:
			togglePaused();
			break;
		case Keys.A:
			psim.showVectors = !psim.showVectors;
			break;
		case Keys.C:
			if (psim.prender.renderMode == RenderModes.AUTO)
				psim.prender.renderMode = RenderModes.MANUAL;
			else {
				psim.prender.renderMode = RenderModes.AUTO;
				psim.prender.zoom = 1;
			}
			break;
		case Keys.G:
			psim.prender.followGrid = !psim.prender.followGrid;
			break;

		case Keys.R:
			reset();
			break;
		case Keys.S:
			save();
			break;
		case Keys.L:
			load();
			break;

		case Keys.LEFT:
			psim.prender.center.x -= ParticleBoxRenderer.checkerWidth * 2
					/ psim.prender.zoom;
			break;
		case Keys.RIGHT:
			psim.prender.center.x += ParticleBoxRenderer.checkerWidth * 2
					/ psim.prender.zoom;
			break;
		case Keys.DOWN:
			psim.prender.center.y -= ParticleBoxRenderer.checkerWidth * 2
					/ psim.prender.zoom;
			break;
		case Keys.UP:
			psim.prender.center.y += ParticleBoxRenderer.checkerWidth * 2
					/ psim.prender.zoom;
			break;

		case Keys.F11:
			if (!isFullscreen) {
				windowedWidth = Gdx.graphics.getWidth();
				windowedHeight = Gdx.graphics.getHeight();
				Gdx.graphics.setDisplayMode(Assets.screenX, Assets.screenY,
						true);
				isFullscreen = true;
			} else {
				Gdx.graphics.setDisplayMode(windowedWidth, windowedHeight,
						false);
				isFullscreen = false;
			}
			break;
		default:

		}

		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (hoverExclusion || suspend)
			return false;
		wasDown = true;
		stage.setKeyboardFocus(null);

		Vector3 unProject = psim.prender.camera.unproject(new Vector3(screenX,
				screenY, 0));

		Particle atPoint = psim
				.particleAt(new Vector2(unProject.x, unProject.y));

		if (atPoint == null) {
			if (orbit && psim.getParticleBox().contains(psim.prender.focus)) {
				Random rand = new Random();
				Particle newParticle = new Particle(psim, getName());
				newParticle.mass = massSet ? mass
						: (psim.maxMass - psim.minMass) * rand.nextFloat()
								+ psim.minMass;
				newParticle.radius = radiusSet ? radius
						: (psim.maxRadius - psim.minRadius) * rand.nextFloat()
								+ psim.minRadius;
				newParticle.position = new Vector2(unProject.x, unProject.y);
				if (colorSet)
					newParticle.color = color;
				psim.addOrbit(focusParticle, newParticle, true);
				spawn(newParticle);
				nextName();
			} else {
				Random rand = new Random();
				PreviewParticle newParticle = new PreviewParticle(psim,
						getName());
				newParticle.mass = massSet ? mass
						: (psim.maxMass - psim.minMass) * rand.nextFloat()
								+ psim.minMass;
				newParticle.radius = radiusSet ? radius
						: (psim.maxRadius - psim.minRadius) * rand.nextFloat()
								+ psim.minRadius;
					newParticle.position = new Vector2(unProject.x, unProject.y);
				newParticle.velocity = new Vector2(0, 0);
				if (colorSet)
					newParticle.color = color;
				psim.prender.setPreviewParticle(newParticle);
				psim.prender.createParticle = true;
			}
		} else {
			if (button == Buttons.RIGHT) {
				psim.remove(atPoint);
			} else {
				setFocus(atPoint);
			}
		}

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if ((hoverExclusion && !wasDown) || suspend)
			return false;
		if (psim.prender.createParticle) {
			Vector3 unProject = psim.prender.camera.unproject(new Vector3(
					screenX, screenY, 0));
			psim.prender.getPreviewParticle().velocity = psim.prender.getPreviewParticle().position
					.cpy().sub(new Vector2(unProject.x, unProject.y));
			Particle newParticle = psim.prender.getPreviewParticle().playParticle();
			psim.add(newParticle);
			spawn(newParticle);
			if (psim.isPaused)
				psim.resetSubFrames();
			psim.prender.createParticle = false;
			nextName();
		}
		wasDown = false;
		return true;
	}

	boolean rpFlag = false;
	Vector2 relPosition;

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if ((hoverExclusion && !wasDown) || suspend)
			return false;
		if (psim.prender.createParticle) {
			Vector3 unProject = psim.prender.camera.unproject(new Vector3(
					screenX, screenY, 0));
			psim.prender.getPreviewParticle().velocity = psim.prender.getPreviewParticle().position
					.cpy().sub(new Vector2(unProject.x, unProject.y));
			/*
			 * if(psim.prender.renderMode == RenderModes.FOCUS) { if(!rpFlag) {
			 * rpFlag = true; relPosition =
			 * psim.prender.previewParticle.position
			 * .cpy().sub(psim.prender.focus.position); }
			 * psim.prender.previewParticle.position =
			 * relPosition.cpy().add(psim.prender.focus.position); }
			 */
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		psim.prender.setZoom(psim.prender.zoom + amount / -6F);
		return false;
	}

	int unnamedCount = 0;

	public String getName() {
		if (nameSet)
			return name;
		else
			return "Unnamed_" + unnamedCount;
	}

	public void nextName() {
		if (!nameSet)
			unnamedCount++;
		lblSetName.setText("Name*:");
		txtSetName
				.setMessageText(unnamedCount > 99 ? ("Unnamed_" + unnamedCount)
						.substring(0, 10) : "Unnamed_" + unnamedCount);

		nameSet = false;
	}
}
