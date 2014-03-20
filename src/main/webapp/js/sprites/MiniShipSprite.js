/**
 * Created by Tim on 20/03/14.
 */
var MiniShipExtendedSprite = function (game, colonyXSprite, planetXSpritesByLetter,  miniShipListener, miniShipImage, colonyGroup ) {
    var planet = planetXSpritesByLetter[ colonyXSprite.colony.planetName].planet;
    Phaser.Sprite.call(this, game,planet.x -33, planet.y-60, miniShipImage);
    colonyGroup.add(this);
    this.colonyXSprite = colonyXSprite;
    this.visible = false;
    this.inputEnabled = true;
    this.events.onInputDown.add(miniShipListener, this);





};

MiniShipExtendedSprite.prototype = Object.create(Phaser.Sprite.prototype);
MiniShipExtendedSprite.prototype.constructor = MiniShipExtendedSprite;