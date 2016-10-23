package com.particlesim.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.particlesim.errorHandling.BadVersionException;
import com.particlesim.graphics.PreviewParticle;
import com.particlesim.physics.Particle;
import com.particlesim.physics.ParticleSim2D;
import com.particlesim.physics.math.Vector2;

public class Automated {

	static ParticleSim2D psim;
	static String base = "<version>0.1.2.3</version><Particles>[Center,1.000001E7,20.2237491607666,1.8465308834087418E-6,0.0017648740629356244,1.3968405726757585E-4,7.23579906189534E-4,139508.19691582336,-4.374318043019775];[Unnamed_4,10.0,3.0,40.0,0.0,0.022200633400275872,500.0012343904587,-62499.68314597551,2.757618854802207];[Unnamed_5,10.0,3.0,60.0,0.0,0.012148143596890579,408.2494283089699,-27777.675373744387,0.81707221688152];[Unnamed_6,10.0,3.0,80.0,0.0,0.00793940723569005,353.5544716776414,-15624.950885632155,0.34470233373631853];[Unnamed_7,10.0,3.0,100.0,0.0,0.0057207116137978535,316.22880867010304,-9999.977795189932,0.17648759247520418];[Unnamed_9,10.0,3.0,120.0,0.0,0.00438531554737359,288.67614902391176,-6944.4416003547885,0.10213402249476317];[Unnamed_11,10.0,3.0,140.0,0.0,0.0035088477346752727,267.26223448410826,-5102.05605173808,0.06431763473842961];[Unnamed_13,10.0,3.0,160.0,0.0,0.002897302585704952,250.00097499957153,-3906.2865651442603,0.04308779024882616];[Unnamed_14,10.0,3.0,180.0,0.0,0.0024507131033955154,235.70322086954772,-3086.48835164831,0.03026193213083249];[Unnamed_15,10.0,3.0,200.0,0.0,0.0021128752458923765,223.60774595575515,-2500.1344013843473,0.022060948456087616];[Unnamed_16,10.0,3.0,220.0,0.0,0.0018500149458432354,213.20165402056801,-2066.502745011571,0.016574717055586354];</Particles><render>0.2238721261469447</render>";
	static Random rand;
	
	static String basePath = new File("").getAbsolutePath();

	public static void main(String[] args) {
		rand = new Random();

		for (int trial = 1; trial < 31; trial++) {
			reload(trial);
			randomStart();
			randomParticle(trial);
			run();
			save(trial);
		}
	}

	public static void reload(int trialNo) {
		System.out.println("Starting Trial #" + trialNo);
		psim = new ParticleSim2D();
		try {
			psim.parseInto(base);
		} catch (BadVersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void randomStart() {
		System.out.println("Setting up...");
		float wait = rand.nextFloat() * 90F + 30F;
		for (float time = 0; time < wait; time += 0.0001F) {
			psim.update(0.0001F);
		}
	}

	public static void randomParticle(int trialNo) {
		Particle newParticle = new PreviewParticle(psim, "Interupptor");
		newParticle.mass = (psim.maxMass - psim.minMass) * rand.nextFloat()
				+ psim.minMass;
		newParticle.radius = (psim.maxRadius - psim.minRadius)
				* rand.nextFloat() + psim.minRadius;
		newParticle.position = new Vector2(rand.nextFloat() * 100 + 100,
				rand.nextFloat() * 50 - 25);
		psim.addOrbit(psim.getParticle("Center"), newParticle, true);
		
		String save = psim.ToString();
		try {
			File file = new File(basePath+"/Trials/Trial_" + trialNo + "_INITIAL.psim");
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				new File("Trials").mkdir();
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(save);
			bw.close();

			System.out.println("Saved to " + file.getAbsoluteFile());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void run() {
		System.out.println("Running...");
		for (float time = 0; time < 600F; time += 0.0001F) {
			psim.update(0.0001F);
		}
	}

	public static void save(int trialNo) {
		String save = psim.ToString();

		try {
			File file = new File(basePath+"/Trials/Trial_" + trialNo + "_FINAL.psim");
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(save);
			bw.close();

			System.out.println("Saved to " + file.getAbsoluteFile());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
