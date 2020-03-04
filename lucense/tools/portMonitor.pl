use IO::Socket::INET;
use HTML::Entities;
use URI::Escape;
use LWP 5.64;
use Data::Dumper;
if($#ARGV == -1 || $#ARGV == 0){
  print "\nUsage : perl \$RESDEX_HOME/current/dep/lucense/tools/portMonitor.pl \$RESDEX_HOME [--force-kill] <port1> [<port2>]";
  print "\nMinimum 1 port expected for monitoring\n";
  exit;
}

$RESDEX_HOME="$ARGV[0]";
do "$RESDEX_HOME/current/dep/lucense/tools/sms.pl";
$MON_LOG="portMonitor.log";
$MON_LOCK="portMonitor.lock";
$MON_LOCK_ERR="portMonitor.err";
$smsList = `cat $RESDEX_HOME/current/etc/monitor.smsable`;
chomp $smsList;
$phpPath = `cat $RESDEX_HOME/current/etc/php.path`;
chomp $phpPath;
my @mobiles = split(/ /, $smsList);

$APPName = `hostname`;
chomp($APPName);
$APPName =~ s/^([^\.]+).+$/$1/i;
system("mkdir -p $RESDEX_HOME/monitor");
if (-e "$RESDEX_HOME/monitor/$MON_LOCK") {
  system("date >> $RESDEX_HOME/monitor/$MON_LOCK_ERR");
  $msg="[$APPName] Monitor Lock Found";
  sendSMS($APPName,$msg,@mobiles);
}
else {
  system("date > $RESDEX_HOME/monitor/$MON_LOCK");
  $MAX_TRIES=4;
  $AUTOSTART_PATH="$RESDEX_HOME/current/bin/searchd";
  $CONFIG_PATH="$RESDEX_HOME/current/etc/search.xml";

  @res = `ps ax|grep SearchServerStarter|grep \"$CONFIG_PATH\"`;

  # 9555 pts/1    Sl+  397:24 /home/resdexshell/rdxNSE/jdk1.6.0_17/jre/bin/java -server -Xmx8192m -cp /home/resdexshell/rdxNSE/releases/20100203172237/lib/*:/home/resdexshell/rdxNSE/releases/20100203172237/build/lucense.jar com.adichad.lucense.searchd.SearchServerStarter --config /home/resdexshell/rdxNSE/current/etc/search.xml --port 6100

  $reg = qr/^\s*(\d+)[^:]+(?!com\.adichad\.lucense\.searchd\.SearchServerStarter).*com\.adichad\.lucense\.searchd\.SearchServerStarter.*(?!--port).*--port\s+(\d+)/;

  foreach $line (@res){
    if ($line =~ m/$reg/) {
      $port[$2]=$1;
    }
  }
  $firstport = 1;
  if($ARGV[$firstport] eq "--force-kill") {
    $forceKill = true;
    $firstport++;
  }
  foreach $argnum ($firstport .. $#ARGV) {
    $pingCmd = "$phpPath $RESDEX_HOME/current/dep/lucense/tools/ping.php $RESDEX_HOME/current/dep/lucense 127.0.0.1 $ARGV[$argnum]";
    $restartCmd = "$AUTOSTART_PATH --config $CONFIG_PATH --port $ARGV[$argnum]";
    if($forceKill) {
      system("kill $port[$ARGV[$argnum]]; $restartCmd >> $RESDEX_HOME/logs/$ARGV[$argnum]-dump.log &");
    } else {
      for ($i=1;$i<($MAX_TRIES+1);$i++) {
        $msg = `$pingCmd`;
        chomp $msg;
        if ($msg eq "PONG") {
          last;
        }
        if($i==$MAX_TRIES) {
          if(!$port[$ARGV[$argnum]]){
            system("$restartCmd >> $RESDEX_HOME/logs/$ARGV[$argnum]-dump.log &");
            $AUTOSTART_PORTS.=" $ARGV[$argnum]";
            $restartFlag=1;
          }
          else {
            $PORT_NOT_RESPONDING .= " $ARGV[$argnum]";
            system("kill -9 $port[$ARGV[$argnum]]; $restartCmd >> $RESDEX_HOME/logs/$ARGV[$argnum]-dump.log &");
            $restartFlag=1;
          }
        }
      }
    }
  }


  if($restartFlag){
    $msg= "daemon(s) restarted [down:$AUTOSTART_PORTS, hung:$PORT_NOT_RESPONDING]";
    sendSMS($APPName,"[$APPName] $msg",@mobiles);
  }
  else {
    $msg= "all well";
  }
  system("echo -e \"[`date +%Y-%m-%d\\ %H\\:%M`]: $msg\" >> $RESDEX_HOME/monitor/$MON_LOG");
  system("rm $RESDEX_HOME/monitor/$MON_LOCK");
}

