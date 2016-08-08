function websocketUrl() {
  var l = window.location;
  return ((l.protocol === 'https:') ? 'wss://' : 'ws://') + l.host + '/ws';
}

(function() {
  var d = document;
  var bossesDiv = d.createElement('div');
  d.body.appendChild(bossesDiv);

  var raidsDiv = d.createElement('div');
  d.body.appendChild(raidsDiv);

  var ws = new WebSocket(websocketUrl());
  ws.onopen = function(event) {
    ws.send(JSON.stringify({'type': 'RaidBossesRequest'}));
  };

  ws.onmessage = function(event) {
    var msg = JSON.parse(event.data)
    switch(msg.type) {
    case 'RaidBossesResponse':
      msg.raidBosses.forEach(function(raidBoss) {
        raidBoss.subscribed = false; // This is so hacky

        var img = d.createElement('img');
        img.src = raidBoss.image;
        img.style.opacity = '0.5';
        img.addEventListener('click', function() {
          raidBoss.subscribed = !raidBoss.subscribed;
          var subscribeMsgType = raidBoss.susbcribed ? 'UnsubscribeRequest' : 'SubscribeRequest';
          img.style.opacity = raidBoss.subscribed ? '1.0' : '0.5';
          ws.send(JSON.stringify({'type': subscribeMsgType, 'bossName': raidBoss.name}));
        });
        bossesDiv.appendChild(img);
      });
      break;

    case 'RaidsResponse':
      msg.raids.forEach(function(raid) {
        var raidText = d.createElement('div');
        raidText.innerHTML = [raid.bossName, raid.id, raid.text].join(' ');
        raidsDiv.insertBefore(raidText, raidsDiv.firstChild);
      });
    }
  };
}());

