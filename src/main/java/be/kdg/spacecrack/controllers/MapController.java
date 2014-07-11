package be.kdg.spacecrack.controllers;

import be.kdg.spacecrack.model.game.SpaceCrackMap;
import be.kdg.spacecrack.repositories.IMapFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

@Controller
@RequestMapping("/map")
public class MapController {
    public static final int MAP_HEIGHT = 500;
    public static final int MAP_LENGTH = 800;

    @Autowired
    public IMapFactory mapService;

    public MapController() {}

    public MapController(IMapFactory mapService) {
        this.mapService = mapService;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody SpaceCrackMap getMap() {
        return mapService.getSpaceCrackMap();
    }

}
