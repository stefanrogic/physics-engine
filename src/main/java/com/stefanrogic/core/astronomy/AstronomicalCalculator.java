package com.stefanrogic.core.astronomy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AstronomicalCalculator {
    
    // J2000.0 EPOCH (JANUARY 1, 2000, 12:00 TT)
    private static final LocalDate J2000_EPOCH = LocalDate.of(2000, 1, 1);
    
    // ORBITAL ELEMENTS AT J2000.0 EPOCH (SIMPLIFIED)
    // SOURCE: NASA JPL APPROXIMATE POSITIONS OF THE PLANETS
    
    // MERCURY ORBITAL ELEMENTS
    private static final double MERCURY_MEAN_LONGITUDE = 252.251; // DEGREES AT J2000.0
    private static final double MERCURY_LONGITUDE_OF_PERIHELION = 77.456; // DEGREES
    private static final double MERCURY_MEAN_MOTION = 4.09233445; // DEGREES PER DAY
    
    // VENUS ORBITAL ELEMENTS
    private static final double VENUS_MEAN_LONGITUDE = 181.980; // DEGREES AT J2000.0
    private static final double VENUS_LONGITUDE_OF_PERIHELION = 131.564; // DEGREES
    private static final double VENUS_MEAN_MOTION = 1.60213034; // DEGREES PER DAY
    
    // EARTH ORBITAL ELEMENTS
    private static final double EARTH_MEAN_LONGITUDE = 100.464; // DEGREES AT J2000.0
    private static final double EARTH_LONGITUDE_OF_PERIHELION = 102.938; // DEGREES
    private static final double EARTH_MEAN_MOTION = 0.98560028; // DEGREES PER DAY
    
    // MARS ORBITAL ELEMENTS
    private static final double MARS_MEAN_LONGITUDE = 355.433; // DEGREES AT J2000.0
    private static final double MARS_LONGITUDE_OF_PERIHELION = 336.041; // DEGREES
    private static final double MARS_MEAN_MOTION = 0.52403840; // DEGREES PER DAY
    
    /**
     * CALCULATE CURRENT ORBITAL ANGLE FOR A PLANET BASED ON CURRENT DATE
     */
    public static double getCurrentOrbitalAngle(String planetName) {
        LocalDate currentDate = LocalDate.now();
        long daysSinceJ2000 = ChronoUnit.DAYS.between(J2000_EPOCH, currentDate);
        
        double meanLongitude, longitudeOfPerihelion, meanMotion;
        
        switch (planetName.toUpperCase()) {
            case "MERCURY":
                meanLongitude = MERCURY_MEAN_LONGITUDE;
                longitudeOfPerihelion = MERCURY_LONGITUDE_OF_PERIHELION;
                meanMotion = MERCURY_MEAN_MOTION;
                break;
            case "VENUS":
                meanLongitude = VENUS_MEAN_LONGITUDE;
                longitudeOfPerihelion = VENUS_LONGITUDE_OF_PERIHELION;
                meanMotion = VENUS_MEAN_MOTION;
                break;
            case "EARTH":
                meanLongitude = EARTH_MEAN_LONGITUDE;
                longitudeOfPerihelion = EARTH_LONGITUDE_OF_PERIHELION;
                meanMotion = EARTH_MEAN_MOTION;
                break;
            case "MARS":
                meanLongitude = MARS_MEAN_LONGITUDE;
                longitudeOfPerihelion = MARS_LONGITUDE_OF_PERIHELION;
                meanMotion = MARS_MEAN_MOTION;
                break;
            default:
                throw new IllegalArgumentException("Unknown planet: " + planetName);
        }
        
        // CALCULATE CURRENT MEAN LONGITUDE
        double currentMeanLongitude = meanLongitude + (meanMotion * daysSinceJ2000);
        
        // CALCULATE MEAN ANOMALY (ANGLE FROM PERIHELION)
        double meanAnomaly = currentMeanLongitude - longitudeOfPerihelion;
        
        // NORMALIZE TO 0-360 DEGREES
        meanAnomaly = normalizeAngle(meanAnomaly);
        
        // CONVERT TO RADIANS
        return Math.toRadians(meanAnomaly);
    }
    
    /**
     * NORMALIZE ANGLE TO 0-360 DEGREES
     */
    private static double normalizeAngle(double angleDegrees) {
        angleDegrees = angleDegrees % 360.0;
        if (angleDegrees < 0) {
            angleDegrees += 360.0;
        }
        return angleDegrees;
    }
}
