package lst;
import robocode.*;

import java.awt.Color;

/**
 * BlueSapphire - a robot by Li Shing To
 * SYS-SPH-1X
 * Description:
 * Attempts to Move in and fire on enemy in close range, may attempt to dodge.
 * Basic robot
 */
public class BlueSapphire extends Robot
{
	/**
	 * run: BlueSapphire's default behavior
	 */
	private long lastScan;
	private long scanInterval = 5;
	
	@Override
	public void run() {
		setColors(Color.BLUE,Color.WHITE,Color.YELLOW);
		
		setBulletColor(Color.CYAN);
		
		this.setAdjustGunForRobotTurn(true);
		
		turnRadarRight(360);
		while(true){
			if(this.getTime() - lastScan > scanInterval)
				this.turnRadarRight(360);
			else{
				scan();
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {	
		lastScan = this.getTime();
		
		double absoluteBearing = this.getHeading() + e.getBearing();
		double dist = e.getDistance();
		
		if(dist < 400 && this.getGunHeat() <= 0){			
			
			turnGunRight(
					Math.toDegrees(
								robocode.util.Utils.normalRelativeAngle(
											Math.toRadians(absoluteBearing - getGunHeading())
										)
									)
								);

			
				int power = 1;
				if (dist < 120)
					 power = 3;
				
				fire(power);
		}else{
			double backDist = 350;
			
			this.turnRight(
					Math.toDegrees(
							robocode.util.Utils.normalRelativeAngle(										
										e.getBearingRadians()
									)
								)
							);
			
			ahead(dist > backDist ? 
					dist - backDist 
					: dist - 50);
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		turnLeft(90 - e.getBearing());
		ahead(50);
	}
	
	@Override
	public void onHitWall(HitWallEvent e){
		turnRight(180);
		ahead(100);
	}
	
	@Override
	public void onHitRobot(HitRobotEvent e){
		double absoluteBearing = this.getHeading() + e.getBearing();
		
		turnGunRight(
				Math.toDegrees(
							robocode.util.Utils.normalRelativeAngle(
										Math.toRadians(absoluteBearing - getGunHeading())
									)
								)
							);

		if(Math.round(this.getGunHeading()) == Math.round(absoluteBearing))
			fire(3);
		back(20);
	}
	
}
