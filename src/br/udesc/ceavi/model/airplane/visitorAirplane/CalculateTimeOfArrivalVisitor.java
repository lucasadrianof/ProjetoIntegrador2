
package br.udesc.ceavi.model.airplane.visitorAirplane;

import br.udesc.ceavi.controlller.Utils;
import br.udesc.ceavi.model.airplane.Airplane;

/**
 *
 * @author Avell
 */
public class CalculateTimeOfArrivalVisitor implements VisitorAirplane {

    @Override
    public void visit(Airplane airplane) {
        double distanceToRouteEnd = Utils.getInstance().calculateDistance
        (airplane.getCurrentLocation(), airplane.getRoute().getExitLocation());
        
        double time = 
    }
    
    
}
