/**
 * Created by Tim on 10/03/14.
 */
//region Prototypes
var PlanetExtendedSprite = function (game, x, y, planet, planetListener) {
    var image = game.cache.getImage('planet1');
    var width = image.width;
    var height = image.height;
    var eligibleMove = false;
    Phaser.Sprite.call(this, game, x - width / 2, y - height / 2, 'planet1');
    this.planet = planet;
    this.inputEnabled = true;

    this.events.onInputDown.add(planetListener, this);
    this.colonyXSprite = null;

    this.getConnectedPlanetXSprites = function (planetXSpritesByLetter) {
        var connectedPlanetXSprites = [];
        for (var i = 0; i < this.planet.connectedPlanets.length; i++) {
            var connectedPlanet = this.planet.connectedPlanets[i];
            connectedPlanetXSprites[i] = planetXSpritesByLetter[connectedPlanet.name];
        }
        return connectedPlanetXSprites;
    };

    this.setEligibleMove = function (isEligible) {
        eligibleMove = isEligible;
        if (eligibleMove) {
            this.loadTexture('planet1_selected');
        } else {
            this.loadTexture('planet1');
        }
    }
    this.isEligibleMove = function () {
        return eligibleMove;
    }

}
PlanetExtendedSprite.prototype = Object.create(Phaser.Sprite.prototype);
PlanetExtendedSprite.prototype.constructor = PlanetExtendedSprite;
