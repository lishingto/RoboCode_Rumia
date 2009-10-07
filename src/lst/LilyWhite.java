package lst;

import java.awt.Color;
import java.awt.geom.*;

import robocode.*;
import robocode.util.Utils;

public class LilyWhite extends AdvancedRobot {

	private double preX = 0.0;
	private double preY = 0.0;
	private double missCnt = 0;
	private double hitCnt = 0;
	
	private double lastEn = 0;
	private double lastScan = 0.0;
	
	private double direction = 1;
	
	private double targetCorrection = 1;

	private boolean bulletBlue = true;
	
	@Override
	public void run() {
		setColors(Color.white, Color.white, Color.red, Color.BLUE, Color.white);
		
		this.setAdjustGunForRobotTurn(true);
		this.setAdjustRadarForGunTurn(true);
		this.setAdjustRadarForRobotTurn(true);

		do{
			if(getTime() - lastScan > 5){
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
			scan();
		}while(true);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		
		lastScan = getTime();
		double absBearing = this.getHeadingRadians() + event.getBearingRadians();
		
		double en = event.getEnergy();
		if(lastEn - en > 0 && lastEn - en <= 3.0){
			dodge(absBearing);
			execute();
		}
		
		lastEn = en;
		
		double eVel = event.getVelocity();
		double dist = event.getDistance();	
		
		double enemyX = getX() + dist * Math.sin(absBearing);
		double enemyY = getY() + dist * Math.cos(absBearing);
		
		double meanVel = Point2D.distance(enemyX, enemyY, preX, preY);
		if(eVel < 0)
			meanVel *= -1;
		
		double meanHeading = Math.asin(
					( (enemyX - preX) * Math.sin(Math.PI/2) ) / meanVel
				);
		
		if(Double.isNaN(meanHeading))
			meanHeading = 0.0;
		
		double latVel = meanVel * Math.sin(meanHeading - absBearing);		
		double offset = Math.asin(latVel /  11.0);
		
		setTurnRadarRightRadians(
				1.99 *
				Utils.normalRelativeAngle(
						absBearing - getRadarHeadingRadians()
				)
		);
		
		double gunTurn = absBearing + (offset * targetCorrection ) - getGunHeadingRadians();
		
		setTurnGunRightRadians(
				Utils.normalRelativeAngle(
						gunTurn 
				)
		);
		
		if(Math.abs(gunTurn) <= 5){
			if(bulletBlue){
				setBulletColor(Color.red);
				bulletBlue = false;
			}else{
				setBulletColor(Color.blue);
				bulletBlue = true;
			}
			
			setFire(3.0);
		}
		execute();
		
		preX = enemyX;
		preY = enemyY;
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		double absBearing = this.getHeadingRadians() + event.getBearingRadians();
		dodge(event.getBearingRadians());
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));
		setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()));
	}



	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		missCnt++;
		if(hitCnt+missCnt > 2){
			double ratio = missCnt / (hitCnt+missCnt);
			if(ratio >= 0.5){
				targetCorrection = targetCorrection==1?0.2:1;
				hitCnt = 0;
				missCnt = 0;
			}
		}
	}
	
	@Override
	public void onBulletHit(BulletHitEvent event) {
		hitCnt++;
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		double absBearing = this.getHeadingRadians() + event.getBearingRadians();
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));
		setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()));
	}

	@Override
	public void onHitWall(HitWallEvent event) {
		setAhead(this.getDistanceRemaining() * -1);
		setTurnRight(0);
		direction *= -1;
		execute();
	}
	
	@Override
	public void onWin(WinEvent event) {
		while(true){
			setAhead(Double.POSITIVE_INFINITY);
			setTurnRight(90);
			execute();
		}
	}

	//Custom Functions
	public void dodge(double incomingBearing){
		setTurnRightRadians(Utils.normalRelativeAngle(incomingBearing + (Math.PI/2)));
		setAhead(200 * direction);
	}

	
}
