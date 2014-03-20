/**
 * Created by Tim on 10/03/14.
 */
var ColonyExtendedSprite = function (game, colony, planetName, playerId, image, colonyGroup, planetXSpritesByLetter, colonyListener) {
    var planet = planetXSpritesByLetter[planetName].planet;
    Phaser.Sprite.call(this, game, planet.x - 15, planet.y - 42, image);
    colonyGroup.add(this);
    this.playerId = playerId;
    this.colony = colony;
    this.miniShipSprite = null;

    if (colonyListener != null) {
        this.inputEnabled = true;
        this.events.onInputDown.add(colonyListener, this);
    }

    this.triggerMiniShip = function () {
        this.miniShipSprite.visible = !this.miniShipSprite.visible;
        this.miniShipSprite.inputEnabled = this.miniShipSprite.visible;
    }

};

ColonyExtendedSprite.prototype = Object.create(Phaser.Sprite.prototype);
ColonyExtendedSprite.prototype.constructor = ColonyExtendedSprite;
