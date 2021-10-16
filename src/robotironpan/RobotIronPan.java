/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package robotironpan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import robocode.*;
import robocode.util.Utils;

/**
 *
 * @author Javier Delgado Lucena y Àlex Rocamora Parrillas
 */
public class RobotIronPan extends AdvancedRobot{

    /**
     * @param args the command line arguments
     */
    
    int scannedX = 0;
    int scannedY = 0;
    double direction = 1;
    
    boolean scanned = false;
    
    public void run() {
         
        setTurnRadarRight(Double.POSITIVE_INFINITY); 
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
        wallSmoothing(e);
        scanned = true;
        setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading()));
        setTurnRight(e.getBearing());
        
        if(e.getDistance() > 200) {
            setAhead(e.getDistance());
        }
        else {
            direction = Math.random()*6;
            if(direction <= 2) direction = -1;
            else direction = 1;
            setTurnRight(200 * direction);
            setAhead(900 * direction);
        }
        
        circularTarget(e);
    } 
    
    public void wallSmoothing(ScannedRobotEvent e) {
        
        double goalDirection = e.getBearing()-Math.PI/2*direction;
            
        Rectangle2D fieldRect = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36,
            getBattleFieldHeight()-36);
        while (!fieldRect.contains(getX()+Math.sin(goalDirection)*120, getY()+
                Math.cos(goalDirection)*120))
        {
                goalDirection += direction*.1;	//turn a little toward enemy and try again
        }
        double turn =
            robocode.util.Utils.normalRelativeAngle(goalDirection-getHeadingRadians());
        if (Math.abs(turn) > Math.PI/2)
        {
                turn = robocode.util.Utils.normalRelativeAngle(turn + Math.PI);
                setBack(100);
        }
        else setAhead(100);
        setTurnRightRadians(turn);  
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
        direction = -1;
        setTurnRight(e.getBearing() * - 30);
        setAhead(150);
    }
    
    public void onHitWall(HitWallEvent e) {
        setTurnRight(500);
        setAhead(800);
    }
    
    public void circularTarget(ScannedRobotEvent e) {
        
        double bulletPower = Math.min(3.0,getEnergy());
        double myX = getX();
        double myY = getY();
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
        double oldEnemyHeading = 0.0;
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        double enemyVelocity = e.getVelocity();
        oldEnemyHeading = enemyHeading;

        double deltaTime = 0;
        double battleFieldHeight = getBattleFieldHeight(), 
               battleFieldWidth = getBattleFieldWidth();
        
        double predictedX = enemyX, predictedY = enemyY;
        while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
              Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
                predictedX += Math.sin(enemyHeading) * enemyVelocity;
                predictedY += Math.cos(enemyHeading) * enemyVelocity;
                enemyHeading += enemyHeadingChange;
                if(predictedX < 18.0 
                        || predictedY < 18.0
                        || predictedX > battleFieldWidth - 18.0
                        || predictedY > battleFieldHeight - 18.0){

                        predictedX = Math.min(Math.max(18.0, predictedX), 
                            battleFieldWidth - 18.0);	
                        predictedY = Math.min(Math.max(18.0, predictedY), 
                            battleFieldHeight - 18.0);
                        break;
                }
        }
        double theta = Utils.normalAbsoluteAngle(Math.atan2(
        predictedX - getX(), predictedY - getY()));

        setTurnRadarRightRadians(Utils.normalRelativeAngle(
        absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(
        theta - getGunHeadingRadians()));
        
        if(e.getDistance() < 500 && e.getDistance() > 100) {
            if(getEnergy() > 80){
                fire(2);
            }
            else fire(1);
        }
        else if(e.getDistance() < 100){
            if(getEnergy() > 50) {
                fire(Rules. MAX_BULLET_POWER);
            }
            else {
                fire(2);
            }
        }
    }
    
}
