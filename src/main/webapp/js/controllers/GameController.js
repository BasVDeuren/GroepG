/**
 * Created by Tim on 24/02/14.
 */
var spaceApp = angular.module('spaceApp');
spaceApp.controller("GameController", function ($scope, $templateCache, $translate, Map, Game, Action, ActiveGame, $route, $routeParams, Spinner) {
    $templateCache.removeAll();

    //region Declarations
//Consts not supported internet explorer

    //region Constants
    var BUILDSHIPCOST = 3;
    var COLONIZECOST = 2;
    var MOVECOST = 1;
    var CANVASWIDTH = 930;
    var CANVASHEIGHT = 558;
    var FARRESTPOINTOFCAMERA = 661;
    var MIDPOINTOFCAMERA = 200;
    //endregion
    var game = new Phaser.Game(CANVASWIDTH, CANVASHEIGHT, Phaser.AUTO, 'game', { preload: preload, create: create, update: update, render: render });
    var cursors;
    var xExtraByCamera;
    var yExtraByCamera;
    var firebaseGameRef;
    var player1CommandPointsText;
    var player2CommandPointsText;

    var p;

    var lineGroup;
    var planetGroup;
    var shipGroup;
    var colonyGroup;
    var buttonGroup;
    var miniShipGroup;

    var btnEndTurn;
    $scope.planetXSpritesByLetter = [];
    $scope.colonyXSpritesById = [];
    $scope.selectedSpaceShipXSprite = null;
    $scope.player1CommandPoints = 0;
    $scope.gameName = "";

    $scope.player2CommandPoints = 0;
    $scope.activePlayerCommandPoints = 0;
    var oldTurnEnded = true;
    $scope.player1IsTurnEnded = true;
    $scope.player2IsTurnEnded = true;
    $scope.activePlayerIsTurnEnded = true;
    var otherTurnEnded = true;
    var oldOtherTurnEnded = true;
    var player1TurnEndedText;
    var player2TurnEndedText;
    $scope.activePlayerMiniShipImage = null;
    var player1CommandPointsSprite;
    var player2CommandPointsSprite;
    var player1TurnEndedSprite;
    var player2TurnEndedSprite;

    $scope.game = {
        gameId: "",
        shipXSpritesById: [],
        activePlayerId: "",
        opponentPlayerId: "",
        activePlayerColonyImage: "",
        opponentPlayerColonyImage: ""
    };
    //endregion


//region Lifecycle Methods
    function preload() {
        //   game.load.image('bg', 'assets/SpaceCrackBackground.jpg');
        game.load.spritesheet('button', 'assets/' + $translate('BUTTON') + '.png', 193, 71);
        game.load.image('planet1', 'assets/planet1_small.png');
        game.load.image('planet1_selected', 'assets/planet1_small_selected.png');
        game.load.image('player1spaceship', 'assets/player1spaceship.png');
        game.load.image('miniplayer1spaceship', 'assets/mini_player1spaceship.png');
        game.load.image('player2spaceship', 'assets/player2spaceship.png');
        game.load.image('miniplayer2spaceship', 'assets/mini_player2spaceship.png');
        game.load.image('player1flag', 'assets/player1flag.png');
        game.load.image('player2flag', 'assets/player2flag.png');
        game.load.image('player1castle', 'assets/player1castle.png');
        game.load.image('player2castle', 'assets/player2castle.png');
        game.load.image('winner', 'assets/monkey_winner_icon.png');
    }

    function create() {

        game.world.setBounds(0, 0, 1780, 1000);
        cursors = game.input.keyboard.createCursorKeys();
        drawGame();



        player1CommandPointsSprite = game.add.sprite(0, 0);
        player1CommandPointsSprite.fixedToCamera = true;
        player1CommandPointsText = game.add.text(5, 5, $translate('COMMANDPOINTS') + ": ", { font: '20px Arial', fill: '#33CCFF'});
        player1CommandPointsSprite.addChild(player1CommandPointsText);

        player2CommandPointsSprite = game.add.sprite(0, 0);
        player2CommandPointsSprite.fixedToCamera = true;
        player2CommandPointsText = game.add.text(CANVASWIDTH - 220, 5, $translate('COMMANDPOINTS') + ": ", { font: '20px Arial', fill: '#9933FF'});
        player2CommandPointsSprite.addChild(player2CommandPointsText);


        player1TurnEndedSprite = game.add.sprite(0, 0);
        player1TurnEndedSprite.fixedToCamera = true;
        player1TurnEndedText = game.add.text(5, 35, $translate('TURNSTATE') + ": ", { font: '20px Arial', fill: '#33CCFF'});
        player1TurnEndedSprite.addChild(player1TurnEndedText);

        player2TurnEndedSprite = game.add.sprite(0, 0);
        player2TurnEndedSprite.fixedToCamera = true;
        player2TurnEndedText = game.add.text(CANVASWIDTH - 220, 35, $translate('TURNSTATE') + ": ", { font: '20px Arial', fill: '#9933FF'});
        player2TurnEndedSprite.addChild(player2TurnEndedText);
    }

    function update() {
        if (cursors.up.isDown) {
            game.camera.y -= 4;
        } else if (cursors.down.isDown) {
            game.camera.y += 4;
        }
        if (cursors.left.isDown) {
            game.camera.x -= 4;
        } else if (cursors.right.isDown) {
            game.camera.x += 4;
        }
        xExtraByCamera = game.camera.x;
        yExtraByCamera = game.camera.y;

        if (btnEndTurn != undefined) {
            btnEndTurn.bringToTop();
            player1TurnEndedSprite.bringToTop();
            player1CommandPointsSprite.bringToTop();
            player2TurnEndedSprite.bringToTop();
            player2CommandPointsSprite.bringToTop();
        }

    }

    function render() {

        //
//            miniShipGroup.z=1000;
//            btnEndTurn.bringToTop();

    }

//endregion
    function resetGame() {
        Spinner.spinner.spin(Spinner.target);
        $.blockUI({ message: null });
        ActiveGame.get({gameId: $routeParams.gameId}, applyGameActiveViewModelData, function () {
            Spinner.spinner.stop();
            $.unblockUI();
        });
    }

    function drawGame() {
        //   game.add.sprite(0, 0, 'bg');
        lineGroup = game.add.group();
        lineGroup.z = 0;
        var graphics = game.add.graphics(0, 0, lineGroup);
        graphics.lineStyle(3, 0x999999, 1);
        Map.get(function (data) {
            //assign planets to their sprites;
            drawPlanets(data);
            drawConnections();
            resetGame();
        }, function () {
        });

        function drawPlanets(data) {
            planetGroup = game.add.group();
            planetGroup.z = 1;
            colonyGroup = game.add.group();
            colonyGroup.z = 2;
            shipGroup = game.add.group();
            shipGroup.z = 4;

            miniShipGroup = game.add.group();
            miniShipGroup.z = 1000;
            btnEndTurn = game.add.button(2000, 75, 'button', function () {
            }, this, 2, 1, 0);
            btnEndTurn.events.onInputDown.add(onClickEndTurn);
            btnEndTurn.fixedToCamera = true;
            btnEndTurn.cameraOffset.setTo(CANVASWIDTH / 2 - 92, CANVASHEIGHT - 83);
            //    miniShipGroup.add(btnEndTurn);
            game.world.add(btnEndTurn);
            var planetArray = data.planets;

            for (var planetKey in planetArray) {
                var planet = planetArray[planetKey];

                $scope.planetXSpritesByLetter[planet.name] = new PlanetExtendedSprite(game, planet.x, planet.y, planet, planetListener);
                game.add.text(planet.x, planet.y, planet.name, { font: '15px Arial', fill: '#FF0000'});
                var planetXSprite = $scope.planetXSpritesByLetter[planet.name];
                planetGroup.add(planetXSprite);
            }
            btnEndTurn.bringToTop();

        }

        function drawConnections() {
            for (var key in $scope.planetXSpritesByLetter) {
                var planetXSprite = $scope.planetXSpritesByLetter[key];

                var connectedPlanets = planetXSprite.planet.connectedPlanets;

                for (var cpKey in  connectedPlanets) {
                    graphics.moveTo(planetXSprite.x + 15, planetXSprite.y + 15);
                    var connectedPlanet = connectedPlanets[cpKey];
                    graphics.lineTo(connectedPlanet.x, connectedPlanet.y);
                }
            }
        }
    }


    function applyGameActiveViewModelData(data) {
        Spinner.spinner.stop();
        $.unblockUI();
        $scope.gameName = data.game.name;
        $scope.game.activePlayerId = data.activePlayerId;
        if (data.game.player2.playerId == $scope.game.activePlayerId) {
            game.camera.x = FARRESTPOINTOFCAMERA;
            game.camera.y = MIDPOINTOFCAMERA;
        }
        firebaseGameRef = new Firebase(data.firebaseGameURL);

        firebaseGameRef.on('value', function (snapshot) {
            var gameData = snapshot.val();
            applyGameData(gameData);
            if ($scope.selectedSpaceShipXSprite != null) {
                $scope.selectedSpaceShipXSprite.highlightConnectedPlanets();
            }

        });
    }


    function miniShipListener(miniShipXSprite) {
        if (!$scope.activePlayerIsTurnEnded) {
            if ($scope.activePlayerCommandPoints >= BUILDSHIPCOST) {
                var action = {
                    gameId: $scope.game.gameId,
                    actionType: "BUILDSHIP",
                    colony: miniShipXSprite.colonyXSprite.colony,
                    playerId: $scope.game.activePlayerId
                };
                Action.save(action, function () {
                }, function () {
                    resetGame();
                });
            } else {
                showNotification($translate('INSUFFICIENTCOMMANDPOINTS'), 2500);
            }
        } else {
            showNotification($translate('TURNENDED'), 2500);
        }
    }

    function colonyListener(colonyXSprite) {
        colonyXSprite.triggerMiniShip();
    }

    function spaceshipListener(spaceShipXSprite) {
        selectShip(spaceShipXSprite);
    }

    function planetListener(planetXSprite) {
        if (planetXSprite.isEligibleMove()) {
            if (!$scope.activePlayerIsTurnEnded) {
                var cost;
                if (planetXSprite.colonyXSprite != null) {
                    if (planetXSprite.colonyXSprite.playerId == $scope.game.activePlayerId) {
                        cost = MOVECOST
                    } else {
                        cost = COLONIZECOST;
                    }
                } else {
                    cost = COLONIZECOST;
                }
                if ($scope.activePlayerCommandPoints >= cost) {

                    allPlanetsNormal();

                    $scope.selectedSpaceShipXSprite.ship.planetName = planetXSprite.planet.name;
                    $scope.selectedSpaceShipXSprite.enforcePositionChanges();


                    var action = {
                        gameId: $scope.game.gameId,
                        actionType: "MOVESHIP",
                        ship: $scope.selectedSpaceShipXSprite.ship,
                        destinationPlanetName: planetXSprite.planet.name,
                        playerId: $scope.game.activePlayerId
                    };
                    Action.save(action, function () {
                    }, function () {
                        resetGame();
                    });
                } else {
                    showNotification($translate('INSUFFICIENTCOMMANDPOINTS'), 2500);

                }
            } else {
                showNotification($translate('TURNENDED'), 2500);
            }
        }
    }

//endregion

    function selectShip(spaceShipXSprite) {
        $scope.selectedSpaceShipXSprite = spaceShipXSprite;
        if (spaceShipXSprite != null || spaceShipXSprite != undefined) {
            spaceShipXSprite.highlightConnectedPlanets();
        }


    }


    function allPlanetsNormal() {
        for (var key in $scope.planetXSpritesByLetter) {
            $scope.planetXSpritesByLetter[key].setEligibleMove(false);
        }
    }


    function applyGameData(gameData) {
        $scope.game.gameId = gameData.gameId;

        $scope.player1CommandPoints = gameData.player1.commandPoints;
        $scope.player2CommandPoints = gameData.player2.commandPoints;
        $scope.player1IsTurnEnded = gameData.player1.turnEnded;
        $scope.player2IsTurnEnded = gameData.player2.turnEnded;
        var player1IsActive = false;
        var player2IsActive = false;

        oldTurnEnded = $scope.activePlayerIsTurnEnded;
        oldOtherTurnEnded = otherTurnEnded;
        if ($scope.game.activePlayerId == gameData.player1.playerId) {
            $scope.game.opponentPlayerId = gameData.player2.playerId;
            $scope.game.opponentPlayerColonyImage = 'player2castle';
            $scope.game.activePlayerColonyImage = 'player1castle';
            $scope.activePlayerMiniShipImage = 'miniplayer1spaceship';
            $scope.activePlayerCommandPoints = $scope.player1CommandPoints;

            $scope.activePlayerIsTurnEnded = gameData.player1.turnEnded;
            otherTurnEnded = gameData.player2.turnEnded;

            player1IsActive = true;
        } else if ($scope.game.activePlayerId == gameData.player2.playerId) {
            $scope.game.opponentPlayerId = gameData.player1.playerId;
            $scope.game.opponentPlayerColonyImage = 'player1castle';
            $scope.game.activePlayerColonyImage = 'player2castle';
            $scope.activePlayerMiniShipImage = 'miniplayer2spaceship';
            $scope.activePlayerCommandPoints = $scope.player2CommandPoints;

            $scope.activePlayerIsTurnEnded = gameData.player2.turnEnded;
            otherTurnEnded = gameData.player1.turnEnded;
            player2IsActive = true;

        }
        if (oldOtherTurnEnded || oldTurnEnded) {
            if (!$scope.activePlayerIsTurnEnded && !otherTurnEnded) {
                showNotification($translate('TURNHASBEGUN'), 2500)
            }
        }

        drawShips(gameData);
        drawColonies(gameData);
        updateCommandPoints();
        var loserPlayerId = gameData.loserPlayerId;
        if (loserPlayerId > 0) {
            if ($scope.game.activePlayerId == loserPlayerId) {
                showNotification($translate('LOSTTHEGAME'), 0);

            } else {
                p = game.add.emitter(game.world.centerX, 200, 200);
                p.makeParticles('winner');
                p.start(false, 5000, 20);
            }
            for (var colonyKey in $scope.colonyXSpritesById) {
                $scope.colonyXSpritesById[colonyKey].inputEnabled = false;
            }
            for (var shipKey in $scope.game.shipXSpritesById) {
                $scope.game.shipXSpritesById[shipKey].inputEnabled = false;
            }
            $scope.selectedSpaceShipXSprite = undefined;
            allPlanetsNormal();
            btnEndTurn.visible = false;
            btnEndTurn.inputEnabled = false;
        } else {
            btnEndTurn.visible = !$scope.activePlayerIsTurnEnded;
        }

        function drawShips(gameData) {
            for (var key in $scope.game.shipXSpritesById) {
                $scope.game.shipXSpritesById[key].destroy();
            }

            $scope.game.shipXSpritesById = [];
            drawShipsOfPlayer(gameData.player1, 'player1spaceship', player1IsActive);
            drawShipsOfPlayer(gameData.player2, 'player2spaceship', player2IsActive);
        }

        function drawShipsOfPlayer(player, image, isActivePlayer) {
            var ships = player.ships;
            for (var shipKey in ships) {
                var ship = ships[shipKey];
                var shipXSprite = new ShipExtendedSprite(game, ship, player.playerId, image, spaceshipListener, isActivePlayer, shipGroup, $scope.planetXSpritesByLetter);
                $scope.game.shipXSpritesById[ship.shipId] = shipXSprite;
            }
        }

        function drawColonies(gameData) {
            colonyGroup.destroy(true);

            colonyGroup = game.add.group();
            colonyGroup.z = 3;

            for (var key in $scope.planetXSpritesByLetter) {
                $scope.planetXSpritesByLetter[key].colonyXSprite = null;
            }
            $scope.colonyXSpritesById = [];
            if (player2IsActive) {
            }

            drawColoniesOfPlayer(gameData.player2, 'player2castle', player2IsActive, null);
            drawColoniesOfPlayer(gameData.player1, 'player1castle', player1IsActive, null);
        }

        function drawColoniesOfPlayer(player, image, isActive) {

            var colonies = player.colonies;
            for (var colonyKey in colonies) {
                var colony = colonies[colonyKey];
                var colonyXSprite;
                if (isActive) {
                    colonyXSprite = new ColonyExtendedSprite(game, colony, colony.planetName, player.playerId, image, colonyGroup, $scope.planetXSpritesByLetter, colonyListener);
                    colonyXSprite.miniShipSprite = new MiniShipExtendedSprite(game, colonyXSprite, $scope.planetXSpritesByLetter, miniShipListener, $scope.activePlayerMiniShipImage, colonyGroup);
                } else {
                    colonyXSprite = new ColonyExtendedSprite(game, colony, colony.planetName, player.playerId, image, colonyGroup, $scope.planetXSpritesByLetter, null);
                }
                $scope.colonyXSpritesById[colony.colonyId] = colonyXSprite;
                $scope.planetXSpritesByLetter[colony.planetName].colonyXSprite = colonyXSprite;
            }
        }

        //   game.world.sort("z");
    }

    function onClickEndTurn() {
        btnEndTurn.visible = false;
        allPlanetsNormal();
        $scope.selectedSpaceShipXSprite = null;
        var action = {
            gameId: $scope.game.gameId,
            actionType: "ENDTURN",
            playerId: $scope.game.activePlayerId
        };
        Action.save(action, function () {
        }, function () {
            resetGame();
        });
    }

    function updateCommandPoints() {
        player1CommandPointsText.setText($translate('COMMANDPOINTS') + ": " + $scope.player1CommandPoints);
        player2CommandPointsText.setText($translate('COMMANDPOINTS') + ": " + $scope.player2CommandPoints);
        player1TurnEndedText.setText($translate('TURNSTATE') + ": " + ($scope.player1IsTurnEnded ? $translate('TURNENDEDSTATE') : $translate('TURNBUSYSTATE')));
        player2TurnEndedText.setText($translate('TURNSTATE') + ": " + ($scope.player2IsTurnEnded ? $translate('TURNENDEDSTATE') : $translate('TURNBUSYSTATE')));

    }

    function showNotification(string, delay) {
        var sprite = game.add.sprite(0, 0);
        var text = game.add.text(5, 5, string, { font: '20px Arial', fill: '#FF0000', align: "center"});
        sprite.addChild(text);
        sprite.fixedToCamera = true;
        sprite.cameraOffset.setTo(CANVASWIDTH / 2, CANVASHEIGHT / 2);
        if (delay != 0) {
            setTimeout(function () {
                sprite.destroy();
            }, delay);
        }
    }
});
