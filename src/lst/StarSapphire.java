package lst;
import robocode.*;

import java.awt.Color;

/**
 * StarSapphire - a robot by Li Shing To
 * SYS-SPH-2A
 * Description:
 * Attempts to Move in and fire on enemy in close range, may attempt to dodge.
 * Has a better fire power determination but dodges less than BlueSapphire
 */
public class StarSapphire extends Robot
{
	/**
	 * run: StarSapphire's default behavior
	 */
	
	private static final int scanInt = 10;
	
	private long lastScan = 0;
	private double lastScanBearing = 0;
	private long scanInterval = scanInt;
	private long reactiveAngle = 90;
	private double eLastEn = 0;
	private long shots = 0;
	
	private int direction = 1;
	
	@Override
	public void run() {
		setColors(Color.BLUE, Color.BLACK,Color.CYAN);
		
		setBulletColor(Color.CYAN);
		
		this.setAdjustGunForRobotTurn(true);
		this.setAdjustRadarForGunTurn(true);
		
		do{
			scan();
			//Responds faster upon dmged, slow response is for better targetting
			scanInterval = scanInt - Math.round(getEnergy()/10);
			if(this.getTime() - lastScan > scanInterval || lastScan == 0)
				turnRadarRight(lastScanBearing>0?360:-360);
		}while(true);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {	
		lastScan = this.getTime();		
		lastScanBearing = e.getBearing();
		double absoluteBearing = this.getHeading() + lastScanBearing;
		turnRadarRight(getGunTurnAmt(absoluteBearing, this.getRadarHeading()));		
		
		double backDist = 100 + Math.round(100 - getEnergy());		

		double dist = e.getDistance();
		double curEn = e.getEnergy();
		
		boolean isShot = (eLastEn - curEn <= 3 && eLastEn - curEn > 0);
		
		if( getGunHeat() > 0 || isShot || shots > 2 ){
			//If over heat or detect fire
			shots = 0;
			double a, b, compAng;
			a = dist;
			b = Math.sqrt( (a*a) + (backDist*backDist) );
			compAng = ( a * Math.sin( Math.toRadians(90)) ) / b ;
			compAng = Math.asin(compAng);
			compAng = Math.toDegrees(compAng);
			compAng = direction==1?Math.abs(reactiveAngle) - compAng:-compAng;
			
			this.turnRight(
					Math.toDegrees(
							robocode.util.Utils.normalRelativeAngle(										
										e.getBearingRadians() + Math.toRadians(Math.abs(reactiveAngle))
									)
								)
							);
			
			if(getVelocity() < 4 || !isShot)
				ahead(backDist*direction);
			else
				ahead(5*direction);
			
			turnRadarLeft(Math.toDegrees(
					robocode.util.Utils.normalRelativeAngle(										
							Math.toRadians(compAng)
						)
					)
				);
			
			if(reactiveAngle != 90 || reactiveAngle != 90)
				reactiveAngle = (direction) * 90;
		}else{
			//If in range
			
			if(dist > 500){							
				this.turnRight(
						Math.toDegrees(
								robocode.util.Utils.normalRelativeAngle(										
											e.getBearingRadians()
										)
									)
								);
				direction = 1;
				ahead(dist > backDist ? 
						dist/2 
						: dist - backDist);
			}else{
				double amtToTurn = getGunTurnAmt( absoluteBearing, this.getGunHeading(), 1.05 );		
				turnGunRight(amtToTurn);
				
				//if in range of turret
				double power = 0.5;
				double eV = e.getVelocity();
				
				shots++;
				
				if(amtToTurn <= 18 || eV < 3){
					if (e.getDistance() < 100 || eV < 1)
						 power = 3;
					else
						power = 2;
				}else if(eV < 5){
					power = 1;
				}
				
				fire(power);
				fire(0.5);
			}
		}			
		
		eLastEn = curEn;
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		double absoluteBearing = this.getHeading() + e.getBearing();
		
		double amtToTurn = getGunTurnAmt( absoluteBearing, this.getGunHeading() );
		turnGunRight(amtToTurn);
		turnRadarRight(getGunTurnAmt(absoluteBearing, this.getRadarHeading()));
		
		fire(1);
		scan();
	}
	
	@Override
	public void onHitWall(HitWallEvent e){
		reactiveAngle *= -(Math.round(Math.random()*0.3) + 1);
		direction *= -1;
		ahead(50*direction);
	}
	
	@Override
	public void onHitRobot(HitRobotEvent e){
		double absoluteBearing = this.getHeading() + e.getBearing();
		
		turnGunRight( getGunTurnAmt( absoluteBearing, this.getGunHeading() )	);

		if(Math.round(this.getGunHeading()) == Math.round(absoluteBearing))
			fire(3);
	}
	
	@Override
	public void onWin(WinEvent e){
		turnGunRight(360);
		turnRadarLeft(360);
	}
	
	public double getGunTurnAmt(double absBear, double gunHeading){
		return getGunTurnAmt(absBear, gunHeading, 1.0);
	}
	
	public double getGunTurnAmt(double absBear, double gunHeading, double factor){
		return Math.toDegrees(
				factor *
				robocode.util.Utils.normalRelativeAngle(
							Math.toRadians(absBear- gunHeading)
						)
					);
	}
	
}
