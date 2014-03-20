/**
 * Created by Tim on 10/03/14.
 */
var ShipExtendedSprite = function (game, ship, playerId, image, spaceshipListener, isFromActivePlayer, shipGroup, planetXSpritesByLetter) {
    var planet = planetXSpritesByLetter[ship.planetName].planet;
    Phaser.Sprite.call(this, game, planet.x, planet.y - 15, image);
    shipGroup.add(this);
    var text = game.add.text(34, -5, ship.strength+'', {font: '20px Arial', fill: '#FF0000'});
    this.addChild(text);

    this.ship = ship;
    this.playerId = playerId;

    this.inputEnabled = true;


    text.setText(this.ship.strength);

    if (isFromActivePlayer) {
        this.events.onInputDown.add(spaceshipListener, this);
    }

    this.enforcePositionChanges = function () {
        this.x = planetXSpritesByLetter[this.ship.planetName].planet.x;
        this.y = planetXSpritesByLetter[this.ship.planetName].planet.y - 15;
    };

    this.highlightConnectedPlanets = function () {
        var planetXSprite = planetXSpritesByLetter[this.ship.planetName];
        var connectedPlanetXSprites = planetXSprite.getConnectedPlanetXSprites(planetXSpritesByLetter);
        for (var i = 0; i < connectedPlanetXSprites.length; i++) {
            connectedPlanetXSprites[i].setEligibleMove(true);
        }
    }
};

ShipExtendedSprite.prototype = Object.create(Phaser.Sprite.prototype);
ShipExtendedSprite.prototype.constructor = ShipExtendedSprite;
