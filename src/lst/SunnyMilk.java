package lst;
import robocode.*;

import java.awt.Color;

/**
 * SunnyMilk - a robot by Li Shing To
 * SYS-SMK-0X
 *  
 */
public class SunnyMilk extends Robot
{
	/**
	 * run: SunnyMilk's default behavior
	 */
	private long lastScan;
	private long scanInterval = 5;
	private long missedCnt = 0;
	private long hitCnt = 0;
	
	@Override
	public void run() {
		setColors(Color.WHITE,Color.RED,Color.YELLOW);
		
		setBulletColor(Color.RED);
		this.setScanColor(Color.white);
		
		this.setAdjustGunForRobotTurn(true);
		
		turnRadarRight(360);
		while(true){			
			if(this.getTime() - lastScan > scanInterval)
				this.turnRadarRight(360);
			else{
				scan();
			}
			if(missedCnt >= 3 && missedCnt%3 == 0){
				turnRight(getGunHeading() - getHeading());
				ahead(100);
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
					
		double amtToTurn = getGunTurnAmt( absoluteBearing, this.getGunHeading(), 1.2 );
		
		turnGunRight(amtToTurn);

		if(amtToTurn < 60){
			setBulletColor(Color.green);
			double power = 1.25;
			if(amtToTurn < 20){
				if ((dist < 200 && e.getVelocity() < 2) || hitCnt > 2){
					setBulletColor(Color.red);
					 power = 3;
				}else{
					setBulletColor(Color.ORANGE);
					power = 2;
				}
			}
			
			fire(missedCnt>2?power/2:power);
			fire(0.2);
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		double absoluteBearing = this.getHeading() + e.getBearing();
		double amtToTurn = getGunTurnAmt( absoluteBearing, this.getGunHeading() );
		turnGunRight(amtToTurn);
		fire(0.5);
	}
	
	@Override 
	public void onBulletMissed(BulletMissedEvent e){
		missedCnt++;
		hitCnt = 0;
	}
	
	@Override
	public void onBulletHit(BulletHitEvent e){
		missedCnt = 0;
		hitCnt++;
	}
	
	@Override
	public void onHitWall(HitWallEvent e){
		turnRight(180);
		ahead(100);
	}
	
	@Override
	public void onHitRobot(HitRobotEvent e){
		double absoluteBearing = this.getHeading() + e.getBearing();
		
		turnGunRight( getGunTurnAmt( absoluteBearing, this.getGunHeading() ) );

		if(Math.round(this.getGunHeading()) == Math.round(absoluteBearing))
			fire(3);
		back(20);
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
