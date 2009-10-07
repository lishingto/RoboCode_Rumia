/**
 * 
 */
package lst;
import robocode.*;
import robocode.util.Utils;

import java.awt.Color;

/**
 * MystiaLorelei - a robot by Li Shing To
 * SYS-MYSLRI-1
 * Description:
 * Circles around the enemy and fires continously until the target dies
 */
public class MystiaLorelei extends AdvancedRobot {
	
	private long lastScan = 0;
	private long lastAttach = 0;
	private long lastWallBounce = 0;
	//ATTRIBUTES
	private boolean attached = false;
	private int direction = 1;	
	private double power = 1.0;
	
	//Stage Values
	double stageWidth;
	double stageHeight;
	
	@Override
	public void run(){
		setColors(
					new Color(153, 107, 91), 
					Color.white, 
					Color.RED, 
					Color.magenta, 
					Color.black
				);
		
		this.setAdjustRadarForGunTurn(true);
		this.setAdjustGunForRobotTurn(true);
		this.setAdjustRadarForRobotTurn(true);
		
		turnRadarRightRadians(Double.POSITIVE_INFINITY);	
		
		stageWidth = this.getBattleFieldWidth();
		stageHeight = this.getBattleFieldHeight();
		
		do{
			execute();
			
			//Wall Repulsion Technology
			
			Condition nearWallCondition = new Condition("nearWall") {
			       public boolean test() {
			    	   if(attached && getTime() - lastWallBounce > 50){
				    	   double x = getX(), y = getY(), pred = 100;
				    	   if(x - pred < 0 || x + pred > stageWidth || y - pred < 0 || y + pred > stageHeight){
				    		   return true;
							}else{
								return false;
							}
			    	   }else{
			    		   return false;
			    	   }
			       };
			   };
			
			addCustomEvent(nearWallCondition);
			
			if(getTime() - lastScan > 25){
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
				attached = false;
			}else{			
				scan();
			}
			
		}while(true);
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		
		lastScan = getTime();
		
		//Lock on to enemy
		double eVel = event.getVelocity();
		double enBear = event.getBearingRadians();
		double absBear = this.getHeadingRadians() + enBear;		
		double dist = event.getDistance();		
		double radarAmt = Utils.normalRelativeAngle( absBear - getRadarHeadingRadians() );		
		setTurnRadarRightRadians(1.99*radarAmt);
		
		if(!attached){
			lastAttach = lastScan;
			//Attach to enemy		
			if(dist > 350){
				turnRightRadians(
							Utils.normalRelativeAngle( enBear )
						);
				
				ahead(
						dist - 200
				);
				radarAmt = Utils.normalRelativeAngle( absBear - getRadarHeadingRadians() );		
				setTurnRadarRightRadians(1.99*radarAmt);
			}
			attached = true;
		}else if(lastScan - lastAttach > 30){
			if(dist > 350){
				attached = false;
			}else{
				lastAttach = lastScan;
			}
		}else{
			double eHead = event.getHeading();
			double headingDiffBack = Math.abs( 
						Utils.normalRelativeAngle(
								(eHead - this.getGunHeading()) * (Math.PI/180)
						)
					);
			double headingDiffFront = Math.abs(
					Utils.normalRelativeAngle(
							((eHead>180?eHead-180:eHead + 180) - this.getGunHeading() )
							 * (Math.PI/180)
						)
					);
			
			System.out.println(headingDiffBack + " : " + headingDiffFront);
			if(eVel < 1){
					if(dist > 300){
						power = 2.0;
					}else{
						power = 3.0;
					}
			}else{
				if(headingDiffBack < 35 || headingDiffFront < 35){
					power = 3.0;
				}else{
					power = 1.0;
				}
			}
			
			double currentGunTurnAmt = Utils.normalRelativeAngle( absBear - getGunHeadingRadians() );
			//Guess Factor Targetting
			double zAngle = (Math.PI*2) - (Math.PI - absBear) - event.getHeadingRadians();
			zAngle = Utils.normalRelativeAngle(zAngle);
						
			System.out.println("\n\nzAngle: " +zAngle);
			
			double newAngle = 0;
			if(eVel > 0){
				double eLoc;
				
				long t, f = 1, cnt = 0;
				double newDist, mintf, minELoc = 0, minNewDist = 0, testAngle = 0;
				mintf = Double.POSITIVE_INFINITY;
				
				
				while(cnt < 20){
					eLoc = eVel * f;				
					newDist= Math.sqrt(
								(dist*dist) + (eLoc*eLoc) - 2 * eLoc * dist * Math.cos(zAngle)
							);						
					
					testAngle = Math.acos(
								(Math.pow(dist,2) + Math.pow(minNewDist,2) - Math.pow(minELoc, 2) )
								/(2*dist*minNewDist)
							);					
					if(eLoc < 0)
						testAngle *= -1;
					
					t = Math.round( ( newDist / (20 - (3*power)) ) + (currentGunTurnAmt + testAngle)/Math.toRadians(20) );
					
					System.out.print("F: " + f + " t: " + t);
					
					if(t-f > 2 || t-f < -2){
						f = t-2;
					}				
					if(t-f < mintf){
						mintf = t-f;
						minELoc = eLoc;
						minNewDist = newDist;
						if(mintf == 0)
							break;
					}
					
					cnt++;
					f++;
				}
				newAngle = testAngle;
			}
			
			
			double gunTurnAmt = currentGunTurnAmt + (newAngle * 0.75);
			double turnAmt = event.getBearingRadians() + Math.PI/2;
			setTurnGunRightRadians(
					gunTurnAmt
				);
			execute();
			
			if(Math.abs(gunTurnAmt + turnAmt) <= 5){
				fire(power);
			}
				
			
			setAhead(Double.POSITIVE_INFINITY * direction);
			setTurnRightRadians(turnAmt);
			setTurnRadarLeftRadians(event.getBearingRadians() + Math.PI/2);
		}
		

	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {

	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		double enBear = event.getBearingRadians();
		double absBear = this.getHeadingRadians() + enBear;	
		double radarAmt = Utils.normalRelativeAngle( absBear - getRadarHeadingRadians() );		
		setTurnRadarRightRadians(2*radarAmt);
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		double enBear = event.getBearingRadians();
		double absBear = this.getHeadingRadians() + enBear;		
		double radarAmt = Utils.normalRelativeAngle( absBear - getRadarHeadingRadians() );		
		setTurnRadarRightRadians(2*radarAmt);
		direction *= -1;
	}

	@Override
	public void onHitWall(HitWallEvent event) {
		direction *= -1;
	}

	@Override
	public void onCustomEvent(CustomEvent event) {
		String condName = event.getCondition().getName();
		if( condName.equals("nearWall") ){
			lastWallBounce = getTime();
			direction *= -1;
			setAhead(Double.POSITIVE_INFINITY * direction);
		}
	}
	
	//Custom Functions
}
