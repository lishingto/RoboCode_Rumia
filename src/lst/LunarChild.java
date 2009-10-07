package lst;
import robocode.*;

import java.awt.Color;

/**
 * LunarChild - a robot by Li Shing To
 * SYS-LC-1X
 */
public class LunarChild extends Robot
{
	/**
	 * run: LunarChild's default behavior
	 */
	
	private boolean fired = true;
	private boolean stopped = true;
	private int direction = 1;
	
	@Override
	public void run() {
		setColors(Color.WHITE,Color.YELLOW,Color.BLACK);
		
		setBulletColor(Color.BLACK);
		this.setAdjustGunForRobotTurn(true);
		
		while(true){
			if(fired && stopped){
				fired = false;
				stopped = false;
				
				long turnAmt = Math.round(Math.random()*120)+15;
				if(Math.round(Math.random()*1) == 0)
					turnAmt *= (0-1);
				turnRight(turnAmt);
				ahead(direction * (Math.round(Math.random()*300)+50));		
				direction *= 1;
			}
			
			turnRadarRight(360);
			stopped = true;
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {	
		if(!fired){
			double absoluteBearing = this.getHeading() + e.getBearing();		
			double amtToTurn = getGunTurnAmt(absoluteBearing, this.getGunHeading());
			turnGunRight(amtToTurn);	
			
			double gunHead = Math.round(this.getGunHeading());
			double roundAbs = Math.round(absoluteBearing);
			if( amtToTurn < 20 || (gunHead > roundAbs - 5 && gunHead < roundAbs + 5) ){
				fire(3);
				fire(0.5);
				fired = true;
			}
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		direction *= 2;
		scan();
	}
	
	@Override
	public void onHitWall(HitWallEvent e){
		direction *= -1;
	}
	
	@Override
	public void onHitRobot(HitRobotEvent e){
		double absoluteBearing = this.getHeading() + e.getBearing();
		
		turnGunRight( getGunTurnAmt( absoluteBearing, this.getGunHeading() )	);

		double gunHead = Math.round(this.getGunHeading());
		double roundAbs = Math.round(absoluteBearing);
		
		if(gunHead > roundAbs - 5 && gunHead < roundAbs + 5)
			fire(3);
		back(40);
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
