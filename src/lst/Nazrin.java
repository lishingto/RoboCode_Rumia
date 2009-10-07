package lst;

import robocode.*;
import robocode.util.Utils;

public class Nazrin extends AdvancedRobot {

	@Override
	public void run() {
		this.setAdjustGunForRobotTurn(true);
		this.setAdjustRadarForGunTurn(true);
		this.setAdjustRadarForRobotTurn(true);
		while(true){			
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
			execute();
			scan();
		}
	}
	
	@Override
	public void onBulletHit(BulletHitEvent event) {
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
	}

	@Override
	public void onHitWall(HitWallEvent event) {
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		double absBearing = this.getHeadingRadians() + event.getBearingRadians();
		setTurnRadarRightRadians(
				1.99 *
				Utils.normalRelativeAngle(
						absBearing - getRadarHeadingRadians()
				)
		);
		fire(3);
	}
	
}
