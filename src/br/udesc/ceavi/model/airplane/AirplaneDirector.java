package br.udesc.ceavi.model.airplane;

import br.udesc.ceavi.model.routes.Route;
import java.io.IOException;

/**
 * @author Felipe Lana, Kevin Kons, Lucas Adriano, Jéssica
 */
public class AirplaneDirector {

    private AirplaneBuilder builder;
    
    public AirplaneDirector(AirplaneBuilder builder) {
        this.builder = builder;
    }

    public void build(Route route) throws IOException{
        builder.buildRoute(route);
        builder.buildTotalFuel();
        builder.buildAverageFuelConsumption();
        builder.buildCurrentSpeed();
        builder.buildCurrentHight();
        builder.buildCorrentLocation();
        builder.buildAcceleration();
        builder.buildInclination();
        builder.buildTimeToRouteEnd();
        builder.buildScore();
        builder.buildId();
    }

    public Airplane getAirplane() {
        return builder.getAirplane();
    }
}
