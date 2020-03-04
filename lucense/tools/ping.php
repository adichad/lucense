<?php
require($argv[1]."/api/lucenseapi.php");
$host = $argv[2];
$port = intval($argv[3]);

$cl = new LucenseClient();
$cl->SetServer($host, $port);
$cl->SetTimeout(10000);
print($cl->Ping());

?>
