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
 * Robot creado para la asignatura de PROP 2021 - 2022.
 * @author Javier Delgado Lucena y Àlex Rocamora Parrillas
 */

public class RobotIronPan extends AdvancedRobot{

    
    // Variables globales
    
    private double scannedX = 0;
    private double scannedY = 0;
    private double direction = 1;
    private double oldEnemyHeading = 0.0;
    
    // Constante que define la velocidad de la bala.
    
    private double bulletVelocity = 20 - 3 * Rules.MAX_BULLET_POWER;
    
    /**
     * Método que se ejecuta al empezar la pelea.
     */
    
    public void run() {
        
        setColors(Color.RED, Color.BLACK, Color.WHITE);
        
        setTurnRadarRight(Double.POSITIVE_INFINITY); 
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
    }
    
    /**
     * Función que se ejecuta cuando se escanea al robot rival.
     * @param e Permite obtener información del robot escaneado.
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        
        // Hacemos que el radar se quede fijo en lugar de que se mueva.
        
        setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() 
                + e.getBearing() - getRadarHeading()));
        setTurnRight(e.getBearing());
        
        //Si la distancia es mayor que 200 avanzamos hasta el enemigo. 
        //Una vez cerca hacemos círculos para acorralarlo y dispararle.
        if(e.getDistance() > 200) {
            setAhead(e.getDistance());
        }
        else {
            setTurnRight(200);
            setAhead(900);
        }
        
        circularTarget(e);
    }
    
    /**
     * Función que se ejecuta cuando nuestro robot es golpeado por el rival.
     * @param e Permite obtener información del disparo que hemos recibido.
     */
    public void onHitByBullet(HitByBulletEvent e) {
        
        //Al recibir una bala giramos y cambiamos de dirección.
        direction = -1;
        setTurnRight(e.getBearing() * - 30);
        setAhead(150 * direction);
    }
    
    /**
     * Función que se ejecuta cuando nuestro robot se choca con uno de los bordes.
     * @param e Permite obtener información de la pared que hemos chocado
     */
    public void onHitWall(HitWallEvent e) {
        
        //Cuando chocamos con un muro giramos y avanzamos lo suficiente como para no volver a chocar.
        setTurnRight(500);
        setAhead(800);
    }
    
    /**
     * Función que calcula donde disparar a partir de la posicion del enemigo utilizando una politica de target circular.
     * @param e Permite obtener información del robot escaneado.
     */
    public void circularTarget(ScannedRobotEvent e) {
        
        //Calculamos el ángulo que suma nuestro robot con el del contrincante
        
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        
        //Predecimos las coordenadas de nuestro contrincante.
        scannedX = getX() + e.getDistance() * Math.sin(absoluteBearing);
        scannedY = getY() + e.getDistance() * Math.cos(absoluteBearing);
        
        //Distintas variables de posicionamiento de nuestro contrincante.
        double eHeading = e.getHeadingRadians();
        double eHeadingChange = eHeading - oldEnemyHeading;
        double eVelocity = e.getVelocity();
        oldEnemyHeading = eHeading;
        
        //Llamamos a la función que se encargará de predecir los movimientos.
        predictedData(eHeading, eVelocity, eHeadingChange);
        
        //Ángulo que se calcula respecto a nosotros y la posición del contrincante.
        double angleTheta = Utils.normalAbsoluteAngle(Math.atan2(scannedX - getX(), scannedY - getY()));
        
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(angleTheta - getGunHeadingRadians()));
        
        //Llamamos a la función que se encarga de disparar.
        chooseShot(e);
        
    }
    
    /**
     * Función que calcula la posición en la que puede estar el robot después de ser escaneado para poder
     * disparar con precisión.
     * @param eHeading se trata del rumbo analizado del enemigo en radianes.
     * @param eVelocity se trata de la velocidad analizada del enemigo.
     * @param eHeadingChange se trata de la predicción del rumbo del enemigo comparándola
     * con la anterior.
     */
    public void predictedData(double eHeading, double eVelocity, double eHeadingChange){
        
        //Variable que contará el tiempo en lo que tarda el recorrido del disparo.
        double helpTime = 0;
        
        //Variables predichas anteriormente.
        double predX = scannedX, predY = scannedY;
        
        //Mientras el tiempo que tarda la bala en llegar al enemigo, no sea superior al tiempo que necesita para llegar desde la posicion
        //actual hasta la posición predecida del tanque enemigo, seguimos haciendo predicciones
        while((++helpTime) * bulletVelocity < Point2D.Double.distance(getX(), getY(), predX, predY)){		
                predX += Math.sin(eHeading) * eVelocity;
                predY += Math.cos(eHeading) * eVelocity;
                eHeading += eHeadingChange;
        }
        
        scannedX = predX;
        scannedY = predY;
    }
    
    /**
     * Función que mide la potencia con la que podemos 
     * disparar, dependiendo de la energía qeu tengamos.
     * @param e Contiene información del robot.
     */
    public void chooseShot(ScannedRobotEvent e) {
        
        //Si la distancia es cercana disparamos con fuego 1 o 2.
        if(e.getDistance() < 500 && e.getDistance() > 200) {
            
            //Si la energía es mayor a 80 disparamos más fuerte.
            
            if(getEnergy() > 80){
                fire(2);
            }
            else fire(1);
        }
        else if(e.getDistance() < 200){
            
            // Si estamos cerca y la energía es mayor a 50, aniquilamos.
            if(getEnergy() > 50) {
                fire(Rules.MAX_BULLET_POWER);
            }
            else {
                fire(2);
            }
        }
        
    }
    
}
