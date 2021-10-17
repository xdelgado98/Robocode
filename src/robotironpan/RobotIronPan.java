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
    
    private int scannedX = 0;
    private int scannedY = 0;
    private double direction = 1;
    private double oldEnemyHeading = 0.0;
    
    boolean scanned = false;
    
    public void run() {
        
        setColors(Color.RED, Color.BLACK, Color.WHITE);
        
        setTurnRadarRight(Double.POSITIVE_INFINITY); 
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
        scanned = true;
        setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading()));
        setTurnRight(e.getBearing());
        
        if(e.getDistance() > 200) {
            setAhead(e.getDistance());
        }
        else {
            setTurnRight(200);
            setAhead(900);
        }
        
        circularTarget(e);
    } 
    
    public void onHitByBullet(HitByBulletEvent e) {
        direction = -1;
        setTurnRight(e.getBearing() * - 30);
        setAhead(150 * direction);
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
                if(	predictedX < 18.0 
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
        
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
        
        if(e.getDistance() < 500 && e.getDistance() > 200) {
            if(getEnergy() > 80){
                fire(2);
            }
            else fire(1);
        }
        else if(e.getDistance() < 200){
            if(getEnergy() > 50) {
                fire(Rules.MAX_BULLET_POWER);
            }
            else {
                fire(2);
            }
        }
    }
    
}
