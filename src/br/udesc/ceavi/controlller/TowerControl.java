package br.udesc.ceavi.controlller;

import br.udesc.ceavi.model.airplane.Airplane;
import br.udesc.ceavi.model.airplane.visitorAirplane.CalculateAccelerationVisitor;
import br.udesc.ceavi.model.airplane.visitorAirplane.CalculateNewPosition;
import br.udesc.ceavi.model.airplane.visitorAirplane.CalculateSpeedVisitor;
import br.udesc.ceavi.model.airplane.visitorAirplane.CalculateTimeToRouteEndVisitor;
import br.udesc.ceavi.model.airplane.visitorAirplane.VisitorAirplane;
import br.udesc.ceavi.model.routes.Coordinate;
import br.udesc.ceavi.model.routes.LandingRoute;
import br.udesc.ceavi.utils.AirplaneScoreComparator;
import br.udesc.ceavi.utils.AirplaneTimeComparator;
import br.udesc.ceavi.utils.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Felipe Lana, Kevin Kons, Lucas Adriano, Jéssica
 */
public class TowerControl {

    private ControllerData data;

    private List<Airplane> scoreList = new ArrayList<>(10);
    private List<Airplane> timeList = new ArrayList<>(10);
    private List<Airplane> removeList = new ArrayList<>(10);
    private Map<Integer, Double> timeOnLandingRoute = new HashMap<>(10);
    private VisitorAirplane calculateAcceleration = new CalculateAccelerationVisitor();
    private VisitorAirplane calculateTimeToRouteEnd = new CalculateTimeToRouteEndVisitor();

    public TowerControl() throws IOException, Exception {
        data = new ControllerData();

        for (int i = 0; i < 5; i++) {
            Airplane a = data.buildAirplane();
            scoreList.add(a);
            timeList.add(a);
        }
        scoreList.sort(new AirplaneScoreComparator());
        timeList.sort(new AirplaneTimeComparator());

        System.out.println("Nº de aviões: " + scoreList.size());

        while (!scoreList.isEmpty() && !timeList.isEmpty()) {
            //Garante que as duas Lists estejam ordenadas da igualmente
            arrangeOrdemOfArrival();

            //Move atualiza informações dos aviões
            for (Airplane a : timeList) {
                //If the plane it's ready to go to the landing route
                checkAirplaneCanGetToLandingRoute(a);

                VisitorAirplane calculateNewPosition = new CalculateNewPosition();
                a.accept(calculateNewPosition, Utils.getInstance().getUpdateInterval());
                a.setCurrentLocation((Coordinate) calculateNewPosition.getValue());

                VisitorAirplane calculateSpeed = new CalculateSpeedVisitor();
                a.accept(calculateSpeed);
                a.setCurrentSpeed((double) calculateSpeed.getValue());

                a.accept(calculateTimeToRouteEnd);
                a.setTimeToRouteEnd((double) calculateTimeToRouteEnd.getValue());

                boolean planeMadeIt = a.getTimeToRouteEnd() == 0 || a.isOnTheGround();

                //if the plane it's on the ground, start counting the time to park
                if (a.getRoute() instanceof LandingRoute && planeMadeIt) {
                    
                    double currentTime = timeOnLandingRoute.getOrDefault(a.getId(), (double) 0);
                    double newTime = currentTime + Utils.getInstance().getUpdateInterval();

                    //If the plane still haven't had time to park
                    if (newTime < LandingRoute.TOTAL_TIME_PARKING) {
                        timeOnLandingRoute.put(a.getId(), newTime);
                        System.out.println(String.format("Avião %s está há %s segundos na rota de pouso.", a.getId(), String.valueOf(newTime)));
                    } else {//otherwise, it made it! :D
                        System.out.println(String.format("Avião %s pousou.", a.getId()));
                        removeList.add(a);
                    }
                }
            }

            timeList.removeAll(removeList);
            removeList.clear();
            Thread.sleep(1000);
        }
        System.out.println("Todos aviões pousaram");
        System.exit(0);
    }

    /**
     * Checks if the plane it's low enough to get to the landing route
     *
     * @param plane Current airplane
     */
    private void checkAirplaneCanGetToLandingRoute(Airplane plane) {
        LandingRoute route = data.getLandingRoute();
        
        boolean lowEnough = 
            Math.abs(Math.abs(plane.getCurrentLocation().getLatitude()) - Math.abs(route.getEntryLocation().getLatitude())) <= 300
         && Math.abs(Math.abs(plane.getCurrentLocation().getLongitude()) - Math.abs(route.getEntryLocation().getLongitude())) <= 300;

        boolean airplaneLanding = route.getLastAirplaneInto() != null;
        
        //theres enough for other plane to get into the landing route if the time the second airplane it's bigger
        //than the time to the first one touch the ground and park
        boolean enoughBetweenTime = airplaneLanding
                && ((plane.getTimeToRouteEnd() + LandingRoute.TOTAL_TIME_PARKING) - route.getLastAirplaneInto().getTimeToRouteEnd())
                > LandingRoute.TOTAL_TIME_PARKING;
        
        //the new airplane can get into the landing route if there's no other airplane
        //or if the time to land of the current plane it's bigger then the time of the new plane to land
        if (lowEnough && (!airplaneLanding || enoughBetweenTime) && !route.equals(plane.getRoute())) {
            System.out.println(plane.getCurrentLocation().getLongitude() + " " + plane.getCurrentLocation().getLatitude());
            System.out.println(String.format("Avião %s entrou da rota de pouso", plane.getId()));
            plane.setRoute(data.getLandingRoute());
            route.setLastAirplaneInto(plane);
        }
    }

    //Ensures that the two lists are equally ordered
    private void arrangeOrdemOfArrival() throws Exception {
        for (int i = 0; i < timeList.size(); i++) {
            Airplane airplaneByScore = scoreList.get(i);
            Airplane airplaneByTime = timeList.get(i);

            if (airplaneByScore != airplaneByTime) {
                double scoreListTime = airplaneByScore.getTimeToRouteEnd();

                double newTimeToRouteEnd = scoreListTime + 15;
                airplaneByTime.accept(calculateAcceleration, newTimeToRouteEnd);
                airplaneByTime.setAcceleration((double) calculateAcceleration.getValue());

                airplaneByTime.setTimeToRouteEnd(newTimeToRouteEnd);

                for (int j = i + 1; j < timeList.size(); j++) {
                    airplaneByTime = timeList.get(j);
                    if (airplaneByTime != airplaneByScore) {
                        newTimeToRouteEnd = newTimeToRouteEnd + 15;
                        airplaneByTime.accept(calculateAcceleration, newTimeToRouteEnd);
                        airplaneByTime.setAcceleration((double) calculateAcceleration.getValue());

                        airplaneByTime.setTimeToRouteEnd(newTimeToRouteEnd);
                    }
                }
            }
            scoreList.sort(new AirplaneScoreComparator());
            timeList.sort(new AirplaneTimeComparator());
        }
    }
}